import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Observable, Subject, catchError, forkJoin, map, of, switchMap, takeUntil } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ClientSummary, ClientsService } from '../../core/services/clients.service';
import { ImportService } from '../../core/services/import.service';
import { LoyaltyService } from '../../core/services/loyalty.service';
import {
  AppointmentSummary,
  BookingDataService,
  PurchaseSummary,
  ServiceSummary
} from '../../core/services/booking-data.service';
import { Router } from '@angular/router';

interface ImportStatus {
  type: 'clients' | 'appointments' | 'services' | 'purchases';
  fileName: string;
  status: 'idle' | 'uploading' | 'success' | 'error';
  message?: string;
}

interface LeaderboardEntry {
  clientId: string;
  loyaltyPoints: number;
  fullName: string;
  client?: ClientSummary | null;
}

interface ClientModalData {
  appointments: AppointmentSummary[];
  services: ServiceSummary[];
  purchases: PurchaseSummary[];
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  totalClients = 0;
  enrichedTopClients: LeaderboardEntry[] = [];
  isLoadingTopClients = false;
  clients: ClientSummary[] = [];
  isLoadingClients = false;
  isModalOpen = false;
  isModalLoading = false;
  selectedClient: ClientSummary | null = null;
  modalData: ClientModalData = {
    appointments: [],
    services: [],
    purchases: []
  };

  readonly filterForm = this.fb.nonNullable.group({
    from: [this.toIsoDate(new Date(Date.now() - 30 * 24 * 60 * 60 * 1000))],
    to: [this.toIsoDate(new Date())],
    limit: [5]
  });

  readonly importStatuses: Record<ImportStatus['type'], ImportStatus> = {
    clients: { type: 'clients', fileName: '', status: 'idle' },
    appointments: { type: 'appointments', fileName: '', status: 'idle' },
    services: { type: 'services', fileName: '', status: 'idle' },
    purchases: { type: 'purchases', fileName: '', status: 'idle' }
  };
  readonly importStatusList = Object.values(this.importStatuses);

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly clientsService: ClientsService,
    private readonly importService: ImportService,
    private readonly loyaltyService: LoyaltyService,
    private readonly bookingDataService: BookingDataService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadSummary();
    this.loadClients();
    this.loadTopClients();
    this.filterForm.valueChanges.pipe(takeUntil(this.destroy$)).subscribe(() => this.loadTopClients());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']).catch(() => undefined);
  }

  handleImport(event: Event, type: ImportStatus['type']): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    const status = this.importStatuses[type];
    status.fileName = file.name;
    status.status = 'uploading';
    status.message = undefined;

    const upload$ = this.getImportObservable(type, file);
    upload$.subscribe({
      next: () => {
        status.status = 'success';
        status.message = 'Import completed successfully.';
        this.loadSummary();
        this.loadClients();
      },
      error: () => {
        status.status = 'error';
        status.message = 'Import failed. Please try again.';
      }
    });
  }

  private getImportObservable(type: ImportStatus['type'], file: File): Observable<unknown> {
    switch (type) {
      case 'clients':
        return this.importService.importClients(file);
      case 'appointments':
        return this.importService.importAppointments(file);
      case 'services':
        return this.importService.importServices(file);
      case 'purchases':
        return this.importService.importPurchases(file);
    }
    throw new Error(`Unsupported import type: ${type}`);
  }

  private loadSummary(): void {
    this.clientsService
      .getTotalClients()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: total => (this.totalClients = total),
        error: () => (this.totalClients = 0)
      });
  }

  private loadClients(): void {
    this.isLoadingClients = true;
    this.clientsService
      .getClients()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: clients => {
          this.clients = clients;
          this.isLoadingClients = false;
        },
        error: () => {
          this.clients = [];
          this.isLoadingClients = false;
        }
      });
  }

  private loadTopClients(): void {
    const { from, to, limit } = this.filterForm.getRawValue();
    if (!from || !to) {
      return;
    }

    this.isLoadingTopClients = true;
    this.loyaltyService
      .getTopClients(Number(limit), from, to)
      .pipe(
        switchMap(entries => {
          if (!entries.length) {
            return of<LeaderboardEntry[]>([]);
          }
          const lookups = entries.map(entry =>
            this.clientsService.getClientById(entry.clientId).pipe(
              map(client => ({
                clientId: entry.clientId,
                loyaltyPoints: entry.loyaltyPoints,
                fullName: `${client.firstName} ${client.lastName}`.trim() || client.email,
                client
              })),
              catchError(() =>
                of<LeaderboardEntry>({
                  clientId: entry.clientId,
                  loyaltyPoints: entry.loyaltyPoints,
                  fullName: entry.clientId,
                  client: null
                })
              )
            )
          );
          return forkJoin(lookups);
        }),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: enriched => {
          this.enrichedTopClients = enriched;
          this.isLoadingTopClients = false;
        },
        error: () => {
          this.enrichedTopClients = [];
          this.isLoadingTopClients = false;
        }
      });
  }

  trackClientById(_: number, client: ClientSummary): string {
    return client.id;
  }

  openClientModal(entry: LeaderboardEntry): void {
    this.isModalOpen = true;
    this.isModalLoading = true;
    this.modalData = { appointments: [], services: [], purchases: [] };

    const client$ = entry.client ? of(entry.client) : this.clientsService.getClientById(entry.clientId);

    forkJoin({
      client: client$,
      appointments: this.bookingDataService.getAppointmentsByClient(entry.clientId),
      services: this.bookingDataService.getServicesByClient(entry.clientId),
      purchases: this.bookingDataService.getPurchasesByClient(entry.clientId)
    })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: ({ client, appointments, services, purchases }) => {
          this.selectedClient = client ?? null;
          this.modalData = { appointments, services, purchases };
          this.isModalLoading = false;
        },
        error: () => {
          this.isModalLoading = false;
        }
      });
  }

  closeClientModal(): void {
    this.isModalOpen = false;
    this.selectedClient = null;
    this.modalData = { appointments: [], services: [], purchases: [] };
  }

  getGenderEmoji(gender: string | null | undefined): string {
    if (!gender) {
      return '🙂';
    }
    const normalized = gender.trim().toLowerCase();
    if (normalized.startsWith('m')) {
      return '👨';
    }
    if (normalized.startsWith('f')) {
      return '👩';
    }
    return '🙂';
  }

  get completedImports(): number {
    return this.importStatusList.filter(status => status.status === 'success').length;
  }

  private toIsoDate(date: Date): string {
    const offset = date.getTimezoneOffset();
    const localDate = new Date(date.getTime() - offset * 60 * 1000);
    return localDate.toISOString().split('T')[0] ?? '';
  }
}

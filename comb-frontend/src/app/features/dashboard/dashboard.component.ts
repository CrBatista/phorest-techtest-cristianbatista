import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Observable, Subject, takeUntil } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { ClientsService } from '../../core/services/clients.service';
import { ImportService } from '../../core/services/import.service';
import { LoyaltyService, TopClient } from '../../core/services/loyalty.service';
import { Router } from '@angular/router';

interface ImportStatus {
  type: 'clients' | 'appointments' | 'services' | 'purchases';
  fileName: string;
  status: 'idle' | 'uploading' | 'success' | 'error';
  message?: string;
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  totalClients = 0;
  topClients: TopClient[] = [];
  isLoadingTopClients = false;

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
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadSummary();
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

  private loadTopClients(): void {
    const { from, to, limit } = this.filterForm.getRawValue();
    if (!from || !to) {
      return;
    }

    this.isLoadingTopClients = true;
    this.loyaltyService
      .getTopClients(Number(limit), from, to)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: clients => {
          this.topClients = clients;
          this.isLoadingTopClients = false;
        },
        error: () => {
          this.topClients = [];
          this.isLoadingTopClients = false;
        }
      });
  }

  private toIsoDate(date: Date): string {
    const offset = date.getTimezoneOffset();
    const localDate = new Date(date.getTime() - offset * 60 * 1000);
    return localDate.toISOString().split('T')[0] ?? '';
  }
}

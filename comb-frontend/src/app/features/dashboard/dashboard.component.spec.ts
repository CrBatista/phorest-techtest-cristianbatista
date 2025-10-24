import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { ClientsService } from '../../core/services/clients.service';
import { ImportService } from '../../core/services/import.service';
import { LoyaltyService } from '../../core/services/loyalty.service';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { BookingDataService } from '../../core/services/booking-data.service';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: { logout: jasmine.createSpy('logout') } },
        { provide: ClientsService, useValue: { getTotalClients: () => of(42), getClients: () => of([{
          id: 'client-1',
          firstName: 'Jane',
          lastName: 'Doe',
          email: 'jane@example.com',
          phone: '123456789',
          gender: 'Female'
        }]), getClientById: () => of({
          id: 'client-1',
          firstName: 'Jane',
          lastName: 'Doe',
          email: 'jane@example.com',
          phone: '123456789',
          gender: 'Female'
        }) } },
        { provide: ImportService, useValue: { importClients: () => of({}), importAppointments: () => of({}), importServices: () => of({}), importPurchases: () => of({}) } },
        { provide: LoyaltyService, useValue: { getTopClients: () => of([]) } },
        { provide: BookingDataService, useValue: { getAppointmentsByClient: () => of([]), getServicesByClient: () => of([]), getPurchasesByClient: () => of([]) } },
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates component', () => {
    expect(component).toBeTruthy();
  });

  it('maps gender to emoji', () => {
    expect(component.getGenderEmoji('Female')).toBe('👩');
    expect(component.getGenderEmoji('Male')).toBe('👨');
    expect(component.getGenderEmoji('')).toBe('🙂');
  });
});

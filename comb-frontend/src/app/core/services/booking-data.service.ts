import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from './api.service';

interface PageResponse<T> {
  content: T[];
}

export interface AppointmentSummary {
  appointmentId: string;
  clientId: string;
  startTime: string;
  endTime: string;
}

export interface ServiceSummary {
  id: string;
  appointmentId: string;
  clientId: string;
  name: string;
  price: number;
  loyaltyPoints: number;
  performedAt: string;
}

export interface PurchaseSummary {
  id: string;
  appointmentId: string;
  clientId: string;
  name: string;
  price: number;
  loyaltyPoints: number;
  performedAt: string;
}

@Injectable({ providedIn: 'root' })
export class BookingDataService {
  constructor(private readonly api: ApiService) {}

  getAppointmentsByClient(clientId: string, size = 100): Observable<AppointmentSummary[]> {
    return this.api
      .get<PageResponse<AppointmentSummary>>('/appointments', { page: 0, size })
      .pipe(map(page => page.content.filter(item => item.clientId === clientId)));
  }

  getServicesByClient(clientId: string, size = 100): Observable<ServiceSummary[]> {
    return this.api
      .get<PageResponse<ServiceSummary>>('/services', { page: 0, size })
      .pipe(map(page => page.content.filter(item => item.clientId === clientId)));
  }

  getPurchasesByClient(clientId: string, size = 100): Observable<PurchaseSummary[]> {
    return this.api
      .get<PageResponse<PurchaseSummary>>('/purchases', { page: 0, size })
      .pipe(map(page => page.content.filter(item => item.clientId === clientId)));
  }
}

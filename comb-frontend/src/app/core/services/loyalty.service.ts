import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface TopClient {
  clientId: string;
  loyaltyPoints: number;
}

@Injectable({ providedIn: 'root' })
export class LoyaltyService {
  constructor(private readonly api: ApiService) {}

  getTopClients(limit: number, from: string, to: string): Observable<TopClient[]> {
    return this.api.get<TopClient[]>('/loyalty/top-clients', { limit, from, to });
  }
}

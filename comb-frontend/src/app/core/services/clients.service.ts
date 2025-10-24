import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from './api.service';

export interface ClientSummary {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  gender: string | null;
  banned?: boolean;
}

interface ClientPage {
  content: ClientSummary[];
  totalElements: number;
}

@Injectable({ providedIn: 'root' })
export class ClientsService {
  constructor(private readonly api: ApiService) {}

  getTotalClients(): Observable<number> {
    return this.api
      .get<ClientPage>('/clients', { page: 0, size: 1 })
      .pipe(map(response => response.totalElements));
  }

  getClients(limit = 6): Observable<ClientSummary[]> {
    return this.api
      .get<ClientPage>('/clients', { page: 0, size: limit })
      .pipe(map(response => response.content));
  }
}

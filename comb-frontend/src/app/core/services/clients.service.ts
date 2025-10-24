import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiService } from './api.service';

interface ClientPage {
  content: unknown[];
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
}

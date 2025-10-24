import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface ImportSummary {
  processed: number;
  created: number;
  updated: number;
  skipped: number;
}

@Injectable({ providedIn: 'root' })
export class ImportService {
  constructor(private readonly api: ApiService) {}

  importClients(file: File): Observable<ImportSummary> {
    return this.upload('/import/clients', file);
  }

  importAppointments(file: File): Observable<ImportSummary> {
    return this.upload('/import/appointments', file);
  }

  importServices(file: File): Observable<ImportSummary> {
    return this.upload('/import/services', file);
  }

  importPurchases(file: File): Observable<ImportSummary> {
    return this.upload('/import/purchases', file);
  }

  private upload(path: string, file: File): Observable<ImportSummary> {
    const formData = new FormData();
    formData.append('file', file, file.name);
    return this.api.postMultipart(path, formData);
  }
}

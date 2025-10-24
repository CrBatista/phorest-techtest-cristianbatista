import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private readonly http: HttpClient) {}

  get<T>(path: string, params?: Record<string, string | number | boolean | undefined>): Observable<T> {
    const options = params ? { params: this.buildParams(params) } : {};
    return this.http.get<T>(`${environment.apiBaseUrl}${path}`, options);
  }

  post<T>(path: string, body: unknown): Observable<T> {
    return this.http.post<T>(`${environment.apiBaseUrl}${path}`, body);
  }

  postMultipart<T>(path: string, formData: FormData): Observable<T> {
    return this.http.post<T>(`${environment.apiBaseUrl}${path}`, formData);
  }

  private buildParams(params: Record<string, string | number | boolean | undefined>): HttpParams {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value === undefined || value === null) {
        return;
      }
      httpParams = httpParams.set(key, String(value));
    });
    return httpParams;
  }
}

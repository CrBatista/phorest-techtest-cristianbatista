import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

interface AuthRequest {
  username: string;
  password: string;
}

interface AuthResponse {
  token: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'comb_token';

  constructor(private readonly http: HttpClient) {}

  login(payload: AuthRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${environment.apiBaseUrl}/auth/token`, payload)
      .pipe(tap(response => this.persistToken(response.token)));
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
  }

  get token(): string | null {
    return localStorage.getItem(this.storageKey);
  }

  isAuthenticated(): boolean {
    return !!this.token;
  }

  private persistToken(token: string): void {
    localStorage.setItem(this.storageKey, token);
  }
}

import { Service, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap, tap } from 'rxjs';
import { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from './auth.model';
import { environment } from '../../environments/environment';

const TOKEN_KEY = 'la-ruta.token';

@Service()
export class AuthService {
    private readonly http = inject(HttpClient);

    private readonly token = signal<string | null>(localStorage.getItem(TOKEN_KEY));
    readonly isAuthenticated = computed(() => this.token() !== null);

    register(request: RegisterRequest): Observable<LoginResponse> {
        return this.http
            .post<RegisterResponse>(`${environment.apiUrl}/api/auth/register`, request)
            .pipe(switchMap(() => this.login(request)));
    }

    login(credentials: LoginRequest): Observable<LoginResponse> {
        return this.http
            .post<LoginResponse>(`${environment.apiUrl}/api/auth/login`, credentials)
            .pipe(tap(response => this.saveToken(response.accessToken)));
    }

    logout(): void {
        localStorage.removeItem(TOKEN_KEY);
        this.token.set(null);
    }

    getToken(): string | null {
        return this.token();
    }

    private saveToken(token: string): void {
        localStorage.setItem(TOKEN_KEY, token);
        this.token.set(token);
    }
}
import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { AuthResponse } from './models';

const TOKEN_KEY = 'inkwell_token';
const USER_KEY  = 'inkwell_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private _token  = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private _user   = signal<{ userId: number; username: string } | null>(
    JSON.parse(localStorage.getItem(USER_KEY) ?? 'null')
  );

  readonly token      = this._token.asReadonly();
  readonly currentUser = this._user.asReadonly();
  readonly isLoggedIn  = computed(() => !!this._token());

  constructor(private http: HttpClient, private router: Router) {}

  register(username: string, email: string, password: string) {
    return this.http.post<AuthResponse>('/api/auth/register', { username, email, password })
      .pipe(tap(r => this.persist(r)));
  }

  login(username: string, password: string) {
    return this.http.post<AuthResponse>('/api/auth/login', { username, password })
      .pipe(tap(r => this.persist(r)));
  }

  logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this._token.set(null);
    this._user.set(null);
    this.router.navigate(['/']);
  }

  updateCurrentUser(username: string) {
    const user = this._user();
    if (user) {
      const updated = { ...user, username };
      localStorage.setItem(USER_KEY, JSON.stringify(updated));
      this._user.set(updated);
    }
  }

  private persist(r: AuthResponse) {
    localStorage.setItem(TOKEN_KEY, r.token);
    localStorage.setItem(USER_KEY, JSON.stringify({ userId: r.userId, username: r.username }));
    this._token.set(r.token);
    this._user.set({ userId: r.userId, username: r.username });
  }
}

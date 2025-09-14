import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';
import { API_BASE } from '../api.config';

export interface AuthResponse { token: string }

@Injectable({ providedIn: 'root' })
export class AuthService {
  private userSubject = new BehaviorSubject<{ email: string | null }>({ email: null });
  user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient) {
    const email = localStorage.getItem('email');
    if (email) this.userSubject.next({ email });
  }

  login(email: string, password: string) {
    return this.http.post<AuthResponse>(`${API_BASE}/auth/login`, { email, password }).pipe(
      tap(res => {
        localStorage.setItem('jwt', res.token);
        localStorage.setItem('email', email);
        this.userSubject.next({ email });
      })
    );
  }

  register(username: string, email: string, password: string) {
    return this.http.post(`${API_BASE}/auth/register`, { username, email, password });
  }

  logout() {
    localStorage.removeItem('jwt');
    localStorage.removeItem('email');
    this.userSubject.next({ email: null });
  }

  isAuthenticated() { return !!localStorage.getItem('jwt'); }
}

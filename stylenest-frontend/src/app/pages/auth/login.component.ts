import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CardModule } from 'primeng/card';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';

@Component({
  standalone: true,
  selector: 'app-login',
  imports: [CommonModule, FormsModule, RouterLink, CardModule, InputTextModule, PasswordModule, ButtonModule],
  template: `
  <p-card header="Login" styleClass="p-mb-3" [ngStyle]="{ 'max-width':'420px','margin':'1rem auto' }">
    <form (ngSubmit)="submit()" #f="ngForm">
      <div class="p-field">
        <label for="login-email">Email</label>
        <input id="login-email" pInputText name="email" [(ngModel)]="email" required />
      </div>
      <div class="p-field">
        <label for="login-password">Password</label>
        <input id="login-password" pPassword name="password" [(ngModel)]="password" [feedback]="false" required />
      </div>
      <button pButton label="Login" [disabled]="f.invalid || loading" type="submit"></button>
      <p style="margin-top:.5rem">Don’t have an account? <a routerLink="/register">Register</a></p>
      <p *ngIf="error" style="color:red;margin-top:.5rem">{{error}}</p>
    </form>
  </p-card>
  `
})
export class LoginComponent {
  email = '';
  password = '';
  loading = false;
  error = '';
  constructor(private auth: AuthService, private router: Router) {}
  submit(){
    this.loading = true; this.error = '';
    this.auth.login(this.email, this.password).subscribe({
      next: () => this.router.navigateByUrl('/products'),
      error: err => { this.error = 'Login failed'; this.loading=false; console.error(err); }
    });
  }
}

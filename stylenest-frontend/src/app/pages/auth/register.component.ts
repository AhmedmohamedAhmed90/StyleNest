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
  selector: 'app-register',
  imports: [CommonModule, FormsModule, RouterLink, CardModule, InputTextModule, PasswordModule, ButtonModule],
  template: `
  <p-card header="Register" [ngStyle]="{ 'max-width':'460px','margin':'1rem auto' }">
    <form (ngSubmit)="submit()" #f="ngForm">
      <div class="p-field"><label for="reg-name">Username</label><input id="reg-name" pInputText name="username" [(ngModel)]="username" required /></div>
      <div class="p-field"><label for="reg-email">Email</label><input id="reg-email" pInputText name="email" [(ngModel)]="email" required /></div>
      <div class="p-field"><label for="reg-password">Password</label><input id="reg-password" pPassword name="password" [(ngModel)]="password" [feedback]="false" required /></div>
      <button pButton label="Create account" [disabled]="f.invalid || loading" type="submit"></button>
      <p style="margin-top:.5rem">Already have an account? <a routerLink="/login">Login</a></p>
      <p *ngIf="error" style="color:red;margin-top:.5rem">{{error}}</p>
    </form>
  </p-card>
  `
})
export class RegisterComponent {
  username=''; email=''; password=''; loading=false; error='';
  constructor(private auth: AuthService, private router: Router) {}
  submit(){
    this.loading=true; this.error='';
    this.auth.register(this.username, this.email, this.password).subscribe({
      next: () => this.auth.login(this.email, this.password).subscribe(() => this.router.navigateByUrl('/products')),
      error: err => { this.error='Register failed'; this.loading=false; console.error(err); }
    });
  }
}

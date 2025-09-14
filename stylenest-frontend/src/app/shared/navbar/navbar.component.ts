import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { NgIf, AsyncPipe } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { MenubarModule } from 'primeng/menubar';
import { ButtonModule } from 'primeng/button';
import { MenuItem } from 'primeng/api';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, NgIf, AsyncPipe, MenubarModule, ButtonModule],
  template: `
    <p-menubar [model]="items" [styleClass]="'app-menubar'">
      <ng-template pTemplate="start">
        <a routerLink="/products" class="brand">StyleNest</a>
      </ng-template>
      <ng-template pTemplate="end">
        <span *ngIf="(auth.user$|async)?.email as email" style="margin-right:1rem">{{email}}</span>
        <button *ngIf="!auth.isAuthenticated()" pButton label="Login" class="p-button-text" (click)="router.navigate(['/login'])"></button>
        <button *ngIf="!auth.isAuthenticated()" pButton label="Register" class="p-button-text" (click)="router.navigate(['/register'])"></button>
        <button *ngIf="auth.isAuthenticated()" pButton label="Logout" class="p-button-text" (click)="logout($event)"></button>
      </ng-template>
    </p-menubar>
  `,
  styles: [
    `.brand{font-weight:600;text-decoration:none;color:#fff}`,
    `:host ::ng-deep .app-menubar{background:#1f2937;color:#fff;border:0}`,
    `:host ::ng-deep .app-menubar .p-menuitem-link{color:#fff}`,
    `:host ::ng-deep .app-menubar .p-button{color:#fff}`
  ]
})
export class NavbarComponent {
  items: MenuItem[] = [
    { label: 'Products', routerLink: '/products' },
    { label: 'Cart', routerLink: '/cart' },
    { label: 'Orders', routerLink: '/orders' },
  ];
  constructor(public auth: AuthService, public router: Router) {}
  logout(e: Event){ e.preventDefault(); this.auth.logout(); }
}

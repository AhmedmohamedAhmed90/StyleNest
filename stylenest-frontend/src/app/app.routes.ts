import { Routes } from '@angular/router';
import { ProductListComponent } from './pages/products/product-list.component';
import { CartPageComponent } from './pages/cart/cart-page.component';
import { LoginComponent } from './pages/auth/login.component';
import { RegisterComponent } from './pages/auth/register.component';
import { OrdersPageComponent } from './pages/orders/orders-page.component';
import { PaymentPageComponent } from './pages/payment/payment-page.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'products' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'products', component: ProductListComponent },
  { path: 'cart', component: CartPageComponent },
  { path: 'orders', component: OrdersPageComponent },
  { path: 'payment/:orderId', component: PaymentPageComponent },
  { path: '**', redirectTo: 'products' }
];

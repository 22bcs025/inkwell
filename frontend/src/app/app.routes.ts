import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/blog/blog-list.component').then(m => m.BlogListComponent) },
  { path: 'blogs/:id', loadComponent: () => import('./features/blog/blog-detail.component').then(m => m.BlogDetailComponent) },
  { path: 'write', loadComponent: () => import('./features/blog/blog-form.component').then(m => m.BlogFormComponent), canActivate: [authGuard] },
  { path: 'blogs/:id/edit', loadComponent: () => import('./features/blog/blog-form.component').then(m => m.BlogFormComponent), canActivate: [authGuard] },
  { path: 'login', loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./features/auth/register.component').then(m => m.RegisterComponent) },
  { path: 'users/:id', loadComponent: () => import('./features/user/profile.component').then(m => m.ProfileComponent) },
  { path: '**', redirectTo: '' },
];

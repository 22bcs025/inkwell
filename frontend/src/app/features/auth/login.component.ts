import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="min-h-[80vh] flex items-center justify-center px-4">
      <div class="w-full max-w-sm">
        <h1 class="font-serif text-3xl text-slate-900 mb-1">Welcome back</h1>
        <p class="text-sm text-slate-500 mb-8">Sign in to continue writing.</p>

        <form (ngSubmit)="submit()" class="space-y-4">
          <input [(ngModel)]="username" name="username" required
            class="input" placeholder="Username">
          <input [(ngModel)]="password" name="password" type="password" required
            class="input" placeholder="Password">

          @if (error()) {
            <p class="text-sm text-rose-600 bg-rose-50 px-3 py-2 rounded-lg">{{ error() }}</p>
          }

          <button type="submit" [disabled]="loading()" class="btn-primary w-full justify-center">
            {{ loading() ? 'Signing in…' : 'Sign in' }}
          </button>
        </form>

        <p class="mt-6 text-center text-sm text-slate-500">
          New here? <a routerLink="/register" class="text-indigo-600 hover:underline">Create an account</a>
        </p>
      </div>
    </div>
  `,
})
export class LoginComponent {
  private auth   = inject(AuthService);
  private router = inject(Router);

  username = '';
  password = '';
  loading  = signal(false);
  error    = signal('');

   submit() {
    this.loading.set(true);
    this.error.set('');
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/']),
      error: e => {
        this.error.set(e.error?.error ?? e.message ?? 'Invalid username or password.');
        this.loading.set(false);
      },
    });
  }
}

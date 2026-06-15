import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="min-h-[80vh] flex items-center justify-center px-4">
      <div class="w-full max-w-sm">
        <h1 class="font-serif text-3xl text-slate-900 mb-1">Join Inkwell</h1>
        <p class="text-sm text-slate-500 mb-8">Write and share your ideas.</p>

        <form (ngSubmit)="submit()" class="space-y-4">
          <input [(ngModel)]="username" name="username" required minlength="3" maxlength="50"
            class="input" placeholder="Username">
          <input [(ngModel)]="email" name="email" type="email" required
            class="input" placeholder="Email">
          <input [(ngModel)]="password" name="password" type="password" required minlength="8"
            class="input" placeholder="Password (min 8 characters)">

          @if (error()) {
            <p class="text-sm text-rose-600 bg-rose-50 px-3 py-2 rounded-lg">{{ error() }}</p>
          }

          <button type="submit" [disabled]="loading()" class="btn-primary w-full justify-center">
            {{ loading() ? 'Creating account…' : 'Create account' }}
          </button>
        </form>

        <p class="mt-6 text-center text-sm text-slate-500">
          Already have an account? <a routerLink="/login" class="text-indigo-600 hover:underline">Sign in</a>
        </p>
      </div>
    </div>
  `,
})
export class RegisterComponent {
  private auth   = inject(AuthService);
  private router = inject(Router);

  username = '';
  email    = '';
  password = '';
  loading  = signal(false);
  error    = signal('');

  submit() {
    this.loading.set(true);
    this.error.set('');
    this.auth.register(this.username, this.email, this.password).subscribe({
      next: () => this.router.navigate(['/']),
      error: e => { 
        if (e.error?.fields) {
          const firstFieldErrorKey = Object.keys(e.error.fields)[0];
          this.error.set(e.error.fields[firstFieldErrorKey]);
        } else {
          this.error.set(e.error?.error ?? 'Registration failed.'); 
        }
        this.loading.set(false); 
      },
    });
  }
}

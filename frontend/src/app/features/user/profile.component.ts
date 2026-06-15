import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { UserResponse } from '../../core/models';
import { TimeAgoPipe } from '../../shared/time.pipe';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule, TimeAgoPipe],
  template: `
    @if (user()) {
      <div class="max-w-2xl mx-auto px-4 py-10">
        <div class="flex items-center gap-4 mb-8">
          <div class="w-16 h-16 rounded-full bg-indigo-100 text-indigo-700 text-2xl font-semibold flex items-center justify-center">
            {{ user()!.username[0].toUpperCase() }}
          </div>
          <div>
            <h1 class="font-serif text-3xl text-slate-900">{{ user()!.username }}</h1>
            <p class="text-sm text-slate-500">Member since {{ user()!.createdAt | timeAgo:'date' }}</p>
          </div>
        </div>

        @if (isSelf()) {
          <div class="card p-6">
            <h2 class="font-medium text-slate-900 mb-4">Account settings</h2>
            <form (ngSubmit)="save()" class="space-y-4">
              <div>
                <label class="text-xs font-medium text-slate-500 mb-1 block">Username</label>
                <input [(ngModel)]="editUsername" name="username" class="input" [placeholder]="user()!.username">
              </div>
              <div>
                <label class="text-xs font-medium text-slate-500 mb-1 block">Email</label>
                <input [(ngModel)]="editEmail" name="email" type="email" class="input" [placeholder]="user()!.email">
              </div>
              <div>
                <label class="text-xs font-medium text-slate-500 mb-1 block">New password</label>
                <input [(ngModel)]="editPassword" name="password" type="password" class="input" placeholder="Leave blank to keep current">
              </div>

              @if (msg()) {
                <p class="text-sm px-3 py-2 rounded-lg"
                   [class.text-green-700]="!isError()" [class.bg-green-50]="!isError()"
                   [class.text-rose-600]="isError()"  [class.bg-rose-50]="isError()">
                  {{ msg() }}
                </p>
              }

              <div class="flex items-center justify-between pt-2">
                <button type="submit" [disabled]="saving()" class="btn-primary">
                  {{ saving() ? 'Saving…' : 'Save changes' }}
                </button>
                <button type="button" (click)="deleteAccount()" class="btn-danger text-sm">
                  Delete account
                </button>
              </div>
            </form>
          </div>
        }
      </div>
    }
  `,
})
export class ProfileComponent implements OnInit {
  private api   = inject(ApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  auth = inject(AuthService);

  user     = signal<UserResponse | null>(null);
  saving   = signal(false);
  msg      = signal('');
  isError  = signal(false);

  editUsername = '';
  editEmail    = '';
  editPassword = '';

  isSelf() { return this.auth.currentUser()?.userId === this.user()?.id; }

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getUser(id).subscribe(u => {
      this.user.set(u);
      this.editUsername = u.username;
      this.editEmail    = u.email;
    });
  }

  save() {
    this.saving.set(true);
    const body: Record<string, string> = {};
    if (this.editUsername !== this.user()!.username) body['username'] = this.editUsername;
    if (this.editEmail    !== this.user()!.email)    body['email']    = this.editEmail;
    if (this.editPassword)                           body['password'] = this.editPassword;

    this.api.updateUser(this.user()!.id, body).subscribe({
      next: u => {
        this.user.set(u);
        this.msg.set('Changes saved.');
        this.isError.set(false);
        this.editPassword = '';
        this.saving.set(false);

        if (body['username'] || body['password']) {
          alert('Credentials changed. Please sign in again.');
          this.auth.logout();
        } else if (this.isSelf()) {
          this.auth.updateCurrentUser(u.username);
        }
      },
      error: e => {
        if (e.error?.fields) {
          const firstKey = Object.keys(e.error.fields)[0];
          this.msg.set(e.error.fields[firstKey]);
        } else {
          this.msg.set(e.error?.error ?? 'Failed to save.');
        }
        this.isError.set(true);
        this.saving.set(false);
      },
    });
  }

  deleteAccount() {
    if (!confirm('Delete your account? This cannot be undone.')) return;
    this.api.deleteUser(this.user()!.id).subscribe(() => {
      this.auth.logout();
      this.router.navigate(['/']);
    });
  }
}

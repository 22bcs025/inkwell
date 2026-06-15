import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';

@Component({
  selector: 'app-blog-form',
  standalone: true,
  imports: [FormsModule, RouterLink],
  template: `
    <div class="max-w-3xl mx-auto px-4 py-10">
      <div class="flex items-center gap-3 mb-8">
        <h1 class="font-serif text-3xl text-slate-900">{{ editId() ? 'Edit post' : 'New post' }}</h1>
        <a routerLink="/" class="btn-ghost ml-auto text-sm">Cancel</a>
      </div>

      <form (ngSubmit)="submit()" class="space-y-4">
        <div>
          <input [(ngModel)]="title" name="title" required maxlength="255"
            class="input text-lg font-medium" placeholder="Post title">
        </div>

        <div>
          <textarea [(ngModel)]="content" name="content" required rows="18"
            class="input resize-y leading-relaxed font-sans text-sm"
            placeholder="Write your post in Markdown…"></textarea>
          <p class="text-xs text-slate-400 mt-1">Markdown is supported.</p>
        </div>

        @if (error()) {
          <p class="text-sm text-rose-600 bg-rose-50 px-3 py-2 rounded-lg">{{ error() }}</p>
        }

        <div class="flex justify-end gap-2 pt-2">
          <button type="submit" [disabled]="!title.trim() || !content.trim() || saving()"
            class="btn-primary">
            {{ saving() ? 'Saving…' : (editId() ? 'Update' : 'Publish') }}
          </button>
        </div>
      </form>
    </div>
  `,
})
export class BlogFormComponent implements OnInit {
  private api    = inject(ApiService);
  private router = inject(Router);
  private route  = inject(ActivatedRoute);

  editId = signal<number | null>(null);
  saving = signal(false);
  error  = signal('');
  title  = '';
  content = '';

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editId.set(Number(id));
      this.api.getBlog(Number(id)).subscribe(b => {
        this.title = b.title;
        this.content = b.content;
      });
    }
  }

  submit() {
    this.saving.set(true);
    this.error.set('');
    const body = { title: this.title.trim(), content: this.content.trim() };
    const req = this.editId()
      ? this.api.updateBlog(this.editId()!, body)
      : this.api.createBlog(body);

    req.subscribe({
      next: b => this.router.navigate(['/blogs', b.id]),
      error: e => {
        if (e.error?.fields) {
          const firstKey = Object.keys(e.error.fields)[0];
          this.error.set(e.error.fields[firstKey]);
        } else {
          this.error.set(e.error?.error ?? 'Something went wrong.');
        }
        this.saving.set(false);
      },
    });
  }
}

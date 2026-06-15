import { Component, inject, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { marked } from 'marked';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { BlogResponse, CommentResponse } from '../../core/models';
import { TimeAgoPipe } from '../../shared/time.pipe';

@Component({
  selector: 'app-blog-detail',
  standalone: true,
  imports: [RouterLink, FormsModule, TimeAgoPipe],
  template: `
    @if (blog()) {
      <div class="max-w-3xl mx-auto px-4 py-10">

        <a routerLink="/" class="inline-flex items-center gap-1 text-sm text-slate-400 hover:text-slate-700 mb-8 transition-colors">
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
          </svg>
          Feed
        </a>

        <h1 class="font-serif text-4xl leading-tight text-slate-900 mb-4">{{ blog()!.title }}</h1>

        <div class="flex items-center gap-3 text-sm text-slate-500 mb-8 border-b border-slate-100 pb-6 flex-wrap">
          <a [routerLink]="['/users', blog()!.authorId]"
             class="font-medium text-slate-700 hover:text-indigo-600 transition-colors">
            {{ blog()!.authorUsername }}
          </a>
          <span>{{ blog()!.createdAt | timeAgo:'date' }}</span>
          @if (blog()!.updatedAt !== blog()!.createdAt) {
            <span class="text-slate-400">· updated {{ blog()!.updatedAt | timeAgo }}</span>
          }
          <span class="ml-auto flex items-center gap-4 text-xs">
            <span class="stat-pill">👁 {{ blog()!.views }}</span>
            <span class="stat-pill">💬 {{ blog()!.commentCount }}</span>
          </span>
        </div>

        <div class="article-body prose prose-slate max-w-none"
             [innerHTML]="html()"></div>

        <div class="mt-8 pt-6 border-t border-slate-100 flex items-center gap-3">
          @if (auth.isLoggedIn()) {
            <button (click)="toggleLike()"
              [class.text-rose-500]="liked()" [class.border-rose-200]="liked()"
              class="btn border border-slate-200 hover:border-rose-200 hover:text-rose-500 transition-colors">
              {{ liked() ? '♥' : '♡' }} {{ blog()!.likes }}
            </button>
          } @else {
            <span class="stat-pill text-sm">♡ {{ blog()!.likes }} likes</span>
          }

          @if (isOwner()) {
            <a [routerLink]="['/blogs', blog()!.id, 'edit']" class="btn-ghost ml-auto text-sm">Edit</a>
            <button (click)="deleteBlog()" class="btn-danger text-sm">Delete</button>
          }
        </div>

        <section class="mt-12">
          <h2 class="font-serif text-2xl text-slate-900 mb-6">
            {{ blog()!.commentCount }} comment{{ blog()!.commentCount !== 1 ? 's' : '' }}
          </h2>

          @if (auth.isLoggedIn()) {
            <form (ngSubmit)="submitComment()" class="mb-8 flex gap-2">
              <textarea [(ngModel)]="commentText" name="comment" rows="2"
                class="input resize-none flex-1 text-sm" placeholder="Add a comment…"></textarea>
              <button type="submit" [disabled]="!commentText.trim()" class="btn-primary self-end">Post</button>
            </form>
          }

          <div class="space-y-5">
            @for (c of comments(); track c.id) {
              <div class="flex gap-3">
                <div class="w-7 h-7 rounded-full bg-slate-100 text-slate-600 text-xs font-semibold flex items-center justify-center flex-shrink-0 mt-0.5">
                  {{ c.authorUsername[0].toUpperCase() }}
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-baseline gap-2 mb-1">
                    <a [routerLink]="['/users', c.authorId]"
                       class="text-sm font-medium text-slate-700 hover:text-indigo-600 transition-colors">{{ c.authorUsername }}</a>
                    <span class="text-xs text-slate-400">{{ c.createdAt | timeAgo }}</span>
                    @if (c.authorId === auth.currentUser()?.userId) {
                      <button (click)="deleteComment(c)" class="text-xs text-rose-400 hover:text-rose-600 ml-auto transition-colors">Delete</button>
                    }
                  </div>
                  @if (editingComment() === c.id) {
                    <div class="flex gap-2">
                      <textarea [(ngModel)]="editText" rows="2" class="input resize-none text-sm flex-1"></textarea>
                      <div class="flex flex-col gap-1">
                        <button (click)="saveEdit(c)" class="btn-primary text-xs px-2">Save</button>
                        <button (click)="editingComment.set(null)" class="btn-ghost text-xs px-2">Cancel</button>
                      </div>
                    </div>
                  } @else {
                    <p class="text-sm text-slate-700 leading-relaxed whitespace-pre-wrap">{{ c.content }}</p>
                    @if (c.authorId === auth.currentUser()?.userId) {
                      <button (click)="startEdit(c)" class="text-xs text-slate-400 hover:text-slate-600 mt-1 transition-colors">Edit</button>
                    }
                  }
                </div>
              </div>
            }
          </div>
        </section>
      </div>
    } @else if (loading()) {
      <div class="max-w-3xl mx-auto px-4 py-10 animate-pulse space-y-4">
        <div class="h-8 bg-slate-100 rounded w-3/4"></div>
        <div class="h-4 bg-slate-100 rounded w-1/3"></div>
        <div class="space-y-2 mt-8">
          @for (_ of [1,2,3,4,5]; track $index) {
            <div class="h-4 bg-slate-100 rounded"></div>
          }
        </div>
      </div>
    }
  `,
})
export class BlogDetailComponent implements OnInit {
  private api      = inject(ApiService);
  private route    = inject(ActivatedRoute);
  private router   = inject(Router);
  private sanitizer = inject(DomSanitizer);
  auth             = inject(AuthService);

  blog           = signal<BlogResponse | null>(null);
  html           = signal<SafeHtml>('');
  comments       = signal<CommentResponse[]>([]);
  loading        = signal(true);
  liked          = signal(false);
  editingComment = signal<number | null>(null);
  commentText    = '';
  editText       = '';

  isOwner() {
    return this.auth.currentUser()?.userId === this.blog()?.authorId;
  }

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.getBlog(id).subscribe(b => {
      this.blog.set(b);
      this.html.set(this.sanitizer.bypassSecurityTrustHtml(marked.parse(b.content) as string));
      this.loading.set(false);
    });
    this.api.getComments(id).subscribe(p => this.comments.set(p.content));
    if (this.auth.isLoggedIn()) {
      this.api.getLikeStatus(id).subscribe(s => this.liked.set(s.liked));
    }
  }

  toggleLike() {
    const b = this.blog()!;
    const obs = this.liked() ? this.api.unlikeBlog(b.id) : this.api.likeBlog(b.id);
    obs.subscribe(updated => { this.blog.set(updated); this.liked.update(v => !v); });
  }

  submitComment() {
    if (!this.commentText.trim()) return;
    this.api.createComment(this.blog()!.id, this.commentText).subscribe(c => {
      this.comments.update(cs => [...cs, c]);
      this.blog.update(b => b ? { ...b, commentCount: b.commentCount + 1 } : b);
      this.commentText = '';
    });
  }

  startEdit(c: CommentResponse) { this.editingComment.set(c.id); this.editText = c.content; }

  saveEdit(c: CommentResponse) {
    this.api.updateComment(c.id, this.editText).subscribe(updated => {
      this.comments.update(cs => cs.map(x => x.id === c.id ? updated : x));
      this.editingComment.set(null);
    });
  }

  deleteComment(c: CommentResponse) {
    this.api.deleteComment(c.id).subscribe(() => {
      this.comments.update(cs => cs.filter(x => x.id !== c.id));
      this.blog.update(b => b ? { ...b, commentCount: b.commentCount - 1 } : b);
    });
  }

  deleteBlog() {
    if (!confirm('Delete this post?')) return;
    this.api.deleteBlog(this.blog()!.id).subscribe(() => this.router.navigate(['/']));
  }
}

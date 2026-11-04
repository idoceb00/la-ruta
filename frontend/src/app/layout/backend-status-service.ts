import { HttpBackend, HttpClient, HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Service, signal } from '@angular/core';
import { catchError, defer, first, map, Observable, of, repeat, Subscription, timeout } from 'rxjs';
import { environment } from '../../environments/environment';

export type BackendStatus = 'checking' | 'waking' | 'woken' | 'ready' | 'unreachable';

const SLOW_AFTER_MS = 2_000;
const ATTEMPT_TIMEOUT_MS = 20_000;
const RETRY_DELAY_MS = 3_000;
const GIVE_UP_AFTER_MS = 90_000;
const WOKEN_SCREEN_MS = 900;

@Service()
export class BackendStatusService {
  // Bypasses the interceptors: the ping must never carry a token
  private readonly http = new HttpClient(inject(HttpBackend));
  private readonly healthUrl = `${environment.apiUrl}/actuator/health`;

  private readonly _status = signal<BackendStatus>('checking');
  readonly status = this._status.asReadonly();

  private readonly _startedAt = signal(Date.now());
  readonly startedAt = this._startedAt.asReadonly();

  readonly showScreen = computed(() => {
    const status = this.status();
    return status === 'waking' || status === 'woken' || status === 'unreachable';
  });

  private subscription?: Subscription;
  private slowTimer?: ReturnType<typeof setTimeout>;

  check(showImmediately = false): void {
    this.subscription?.unsubscribe();
    clearTimeout(this.slowTimer);

    this._startedAt.set(Date.now());
    this._status.set(showImmediately ? 'waking' : 'checking');

    // Only show the screen if the server takes a while: a quick answer means it was already awake
    this.slowTimer = setTimeout(() => {
      if (this._status() === 'checking') {
        this._status.set('waking');
      }
    }, SLOW_AFTER_MS);

    this.subscription = this.waitUntilAwake()
      .pipe(timeout({ first: GIVE_UP_AFTER_MS }))
      .subscribe({
        next: () => this.onAwake(),
        error: () => {
          clearTimeout(this.slowTimer);
          this._status.set('unreachable');
        },
      });
  }

  // Pings the health endpoint again and again until the server answers
  private waitUntilAwake(): Observable<boolean> {
    const attempt$ = defer(() => this.http.get(this.healthUrl)).pipe(
      timeout(ATTEMPT_TIMEOUT_MS),
      map(() => true),
      catchError((error: unknown) => of(isAnswerFromServer(error))),
    );
    return attempt$.pipe(repeat({ delay: RETRY_DELAY_MS }), first(Boolean));
  }

  private onAwake(): void {
    clearTimeout(this.slowTimer);
    if (this._status() === 'waking') {
      // Leave the "ready" state on screen for a moment before closing it
      this._status.set('woken');
      setTimeout(() => this._status.set('ready'), WOKEN_SCREEN_MS);
    } else {
      this._status.set('ready');
    }
  }
}

// Any 4xx comes from Spring itself, so the server is up; 5xx and network errors mean it is not yet
function isAnswerFromServer(error: unknown): boolean {
  return error instanceof HttpErrorResponse && error.status >= 400 && error.status < 500;
}
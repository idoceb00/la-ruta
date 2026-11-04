import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { interval, map } from 'rxjs';
import { BackendStatusService } from '../backend-status-service';
import { Button } from '../../shared/button/button';

type StepState = 'done' | 'active' | 'pending';

// Typical moment (in seconds) each phase ends on a cold start; the last one ends only when the server answers
const STEPS: { label: string; until: number | null }[] = [
  { label: 'Conectar con el servidor', until: 3 },
  { label: 'Encender la máquina en Render', until: 20 },
  { label: 'Arrancar Spring Boot', until: 45 },
  { label: 'Conectar con la base de datos', until: null },
];

@Component({
  selector: 'app-wake-up-screen',
  imports: [Button],
  templateUrl: './wake-up-screen.html',
  styleUrl: './wake-up-screen.css',
})
export class WakeUpScreen {
  private readonly backend = inject(BackendStatusService);

  protected readonly status = this.backend.status;
  protected readonly woken = computed(() => this.status() === 'woken');

  private readonly now = toSignal(interval(1000).pipe(map(() => Date.now())), { initialValue: Date.now() });

  protected readonly elapsed = computed(() =>
    Math.max(0, Math.floor((this.now() - this.backend.startedAt()) / 1000)),
  );

  protected readonly steps = computed(() => {
    const elapsed = this.elapsed();
    const woken = this.woken();
    return STEPS.map((step, index) => {
      const from = index === 0 ? 0 : (STEPS[index - 1].until ?? 0);
      const state: StepState =
        woken || (step.until !== null && elapsed >= step.until) ? 'done' : elapsed >= from ? 'active' : 'pending';
      return { label: step.label, state };
    });
  });

  // Fills quickly at first and slows down, never reaching the top until the server answers
  protected readonly progress = computed(() =>
    this.woken() ? 1 : 0.92 * (1 - Math.exp(-this.elapsed() / 22)),
  );

  protected retry(): void {
    this.backend.check(true);
  }
}
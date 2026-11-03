import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../../auth/auth-service';

// Pages where the "Entrar" link makes no sense
const AUTH_ROUTES = ['/login', '/register'];

@Component({
  selector: 'app-header',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  protected readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  // Current URL, updated after every completed navigation
  private readonly url = toSignal(
    this.router.events.pipe(
      filter((event) => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  protected readonly onAuthPage = computed(() =>
    AUTH_ROUTES.some((route) => this.url().startsWith(route)),
  );

  protected logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
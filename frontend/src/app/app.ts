import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Header } from './layout/header/header';
import { WakeUpScreen } from './layout/wake-up-screen/wake-up-screen';
import { BackendStatusService } from './layout/backend-status-service';

@Component({
  imports: [RouterOutlet, Header, WakeUpScreen],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly backendStatus = inject(BackendStatusService);
}
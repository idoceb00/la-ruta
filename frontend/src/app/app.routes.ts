import { Routes } from '@angular/router';
import { BarList } from './bars/bar-list/bar-list';
import { Login } from './auth/login/login';
import { authGuard } from './auth/auth-guard'

export const routes: Routes = [
    { path: '', component: BarList, canActivate: [authGuard] },
    { path: 'login', component: Login},
    { path: '**', redirectTo: ''}
];

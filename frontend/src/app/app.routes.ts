import { Routes } from '@angular/router';
import { BarList } from './bars/bar-list/bar-list';
import { Login } from './auth/login/login';

export const routes: Routes = [
    { path: '', component: BarList },
    { path: 'login', component: Login},
    { path: '**', redirectTo: ''}
];

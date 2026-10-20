import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import { BarList } from './bars/bar-list/bar-list';
import { authGuard } from './auth/auth-guard'
import { guestGuard } from './auth/guest-guard';

export const routes: Routes = [
    { path: 'login', component: Login, canActivate: [guestGuard]},
    { path: 'register', component: Register, canActivate: [guestGuard]},
    {
        path: '',
        canActivate: [authGuard],
        children: [
            { path: '', component: BarList}
        ]
    },
    { path: '**', redirectTo: ''}
];

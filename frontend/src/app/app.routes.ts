import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import { BarList } from './bars/bar-list/bar-list';
import { authGuard } from './auth/auth-guard'
import { guestGuard } from './auth/guest-guard';
import { CommunityList } from './communities/community-list/community-list';
import { CommunityDetail } from './communities/community-detail/community-detail';
import { BarForm } from './bars/bar-form/bar-form';
import { BarDetail } from './bars/bar-detail/bar-detail';

export const routes: Routes = [
    { path: 'login', component: Login, canActivate: [guestGuard]},
    { path: 'register', component: Register, canActivate: [guestGuard]},
    {
        path: '',
        canActivate: [authGuard],
        children: [
            { path: '', component: BarList, pathMatch: 'full' },
            { path: 'communities', component: CommunityList },
            { path: 'communities/:id', component: CommunityDetail },
            { path: 'bars/new', component: BarForm },
            { path: 'bars/:id', component: BarDetail },
            { path: 'bars/:id/edit', component: BarForm}
        ]
    },
    { path: '**', redirectTo: ''}
];

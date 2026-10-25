import { Routes } from '@angular/router';
import { Login } from './auth/login/login';
import { Register } from './auth/register/register';
import { BarList } from './bars/bar-list/bar-list';
import { authGuard } from './auth/auth-guard'
import { guestGuard } from './auth/guest-guard';
import { CommunityList } from './communities/community-list/community-list';
import { CommunityDetail } from './communities/community-detail/community-detail';

export const routes: Routes = [
    { path: 'login', component: Login, canActivate: [guestGuard]},
    { path: 'register', component: Register, canActivate: [guestGuard]},
    {
        path: '',
        canActivate: [authGuard],
        children: [
            { path: 'communities', component: CommunityList, canActivate: [authGuard] },
            { path: '', redirectTo: 'communities', pathMatch: 'full' },
            { path: 'communities/:id', component: CommunityDetail, canActivate: [authGuard] },
            { path: '', component: BarList }
        ]
    },
    { path: '**', redirectTo: ''}
];

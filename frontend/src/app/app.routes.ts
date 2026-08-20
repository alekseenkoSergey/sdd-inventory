import { Routes } from '@angular/router';
import { LoginComponent } from './auth/components/login/login.component';
import { CallbackComponent } from './auth/components/callback/callback.component';
import { HomeComponent } from './pages/home/home.component';
import { CategoriesPageComponent } from './categories/pages/categories-page.component';
import { LocationListComponent } from './features/locations/location-list/location-list.component';
import { AuthGuard } from './auth/guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'auth/callback', component: CallbackComponent },
  { path: 'home', canActivate: [AuthGuard], component: HomeComponent },
  { path: 'categories', canActivate: [AuthGuard], component: CategoriesPageComponent },
  { path: 'locations', canActivate: [AuthGuard], component: LocationListComponent },
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];

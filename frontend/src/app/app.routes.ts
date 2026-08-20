import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { CalendarComponent } from './calendar/calendar.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { ExamListComponent } from './exams/exam-list/exam-list.component';
import { LayoutComponent } from './shared/layout/layout.component';
import { StudySessionListComponent } from './study-sessions/study-session-list/study-session-list.component';
import { SubjectListComponent } from './subjects/subject-list/subject-list.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [guestGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [guestGuard] },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'subjects', component: SubjectListComponent },
      { path: 'exams', component: ExamListComponent },
      { path: 'study-sessions', component: StudySessionListComponent },
      { path: 'calendar', component: CalendarComponent },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];

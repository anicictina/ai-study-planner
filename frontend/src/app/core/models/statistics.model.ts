import { Exam } from './exam.model';
import { StudySession } from './study-session.model';

export interface DayMinutes {
  date: string;
  minutes: number;
}

export interface SubjectMinutes {
  subjectId: number;
  subjectName: string;
  color: string;
  minutes: number;
}

export interface WeeklyStudy {
  weekStart: string;
  totalMinutes: number;
  byDay: DayMinutes[];
  bySubject: SubjectMinutes[];
}

export interface MaterialProgress {
  subjectId: number;
  subjectName: string;
  color: string;
  totalMaterials: number;
  learnedMaterials: number;
  learnedPercent: number;
}

export interface AttemptScore {
  attemptedAt: string;
  materialTitle: string;
  scorePercent: number;
}

export interface QuizStats {
  averageScorePercent: number;
  recentAttempts: AttemptScore[];
}

export interface StatisticsOverview {
  todaySessions: StudySession[];
  upcomingExams: Exam[];
  weeklyStudy: WeeklyStudy;
  materialProgress: MaterialProgress[];
  quizStats: QuizStats;
}

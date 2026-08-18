export interface UserProfile {
  id: number;
  provider: string;
  email?: string;
  displayName?: string;
  avatarUrl?: string;
}

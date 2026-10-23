export type CommunityRole = 'ADMIN' | 'MEMBER';

export interface Community {
    id: number;
    name: string;
    inviteCode: string;
    role: CommunityRole;
    memberCount: number;
}

export interface CommunityRequest {
    name: string;
}

export interface JoinCommunityRequest {
    inviteCode: string;
}
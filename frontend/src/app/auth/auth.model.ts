export interface LoginRequest {
    username: string;
    password: string;
}

export interface LoginResponse {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
}

export interface RegisterResponse {
    id: number;
    username: string;
}

export type RegisterRequest = LoginRequest;
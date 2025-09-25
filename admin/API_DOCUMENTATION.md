# Ekehi Mobile Admin API Documentation

## Overview

This document describes the REST API endpoints used by the Ekehi Mobile Admin Dashboard.

## Authentication

All API requests (except login) require authentication via JWT token in the Authorization header:

```
Authorization: Bearer <token>
```

### Login
```
POST /api/auth/login
```

**Request Body:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "token": "string",
  "user": {
    "id": "string",
    "name": "string",
    "email": "string",
    "role": "string"
  }
}
```

## Dashboard

### Get Dashboard Statistics
```
GET /api/dashboard/stats
```

**Response:**
```json
{
  "totalUsers": 1250,
  "totalRevenue": 250000,
  "activePresales": 3,
  "pendingWithdrawals": 12
}
```

### Get Chart Data
```
GET /api/dashboard/chart
```

**Response:**
```json
{
  "data": [
    {
      "date": "2023-01-01",
      "users": 45,
      "revenue": 12500
    }
  ]
}
```

## Presale Management

### Get All Presales
```
GET /api/presale
```

**Query Parameters:**
- `page` (optional): Page number
- `limit` (optional): Items per page
- `search` (optional): Search term

**Response:**
```json
{
  "presales": [
    {
      "id": "string",
      "name": "string",
      "startDate": "2023-01-01T00:00:00Z",
      "endDate": "2023-01-31T00:00:00Z",
      "targetAmount": 100000,
      "currentAmount": 75000,
      "status": "active"
    }
  ],
  "total": 15,
  "page": 1,
  "limit": 10
}
```

### Create Presale
```
POST /api/presale
```

**Request Body:**
```json
{
  "name": "string",
  "description": "string",
  "startDate": "2023-01-01T00:00:00Z",
  "endDate": "2023-01-31T00:00:00Z",
  "targetAmount": 100000,
  "tokenPrice": 0.1
}
```

**Response:**
```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "startDate": "2023-01-01T00:00:00Z",
  "endDate": "2023-01-31T00:00:00Z",
  "targetAmount": 100000,
  "tokenPrice": 0.1,
  "currentAmount": 0,
  "status": "upcoming"
}
```

### Get Presale by ID
```
GET /api/presale/{id}
```

**Response:**
```json
{
  "id": "string",
  "name": "string",
  "description": "string",
  "startDate": "2023-01-01T00:00:00Z",
  "endDate": "2023-01-31T00:00:00Z",
  "targetAmount": 100000,
  "tokenPrice": 0.1,
  "currentAmount": 75000,
  "status": "active",
  "participants": [
    {
      "userId": "string",
      "amount": 1000,
      "date": "2023-01-15T00:00:00Z"
    }
  ]
}
```

### Update Presale
```
PUT /api/presale/{id}
```

**Request Body:**
```json
{
  "name": "string",
  "description": "string",
  "startDate": "2023-01-01T00:00:00Z",
  "endDate": "2023-01-31T00:00:00Z",
  "targetAmount": 100000,
  "tokenPrice": 0.1
}
```

### Delete Presale
```
DELETE /api/presale/{id}
```

## Ad Management

### Get All Ads
```
GET /api/ads
```

**Response:**
```json
{
  "ads": [
    {
      "id": "string",
      "title": "string",
      "content": "string",
      "imageUrl": "string",
      "targetUrl": "string",
      "startDate": "2023-01-01T00:00:00Z",
      "endDate": "2023-01-31T00:00:00Z",
      "isActive": true
    }
  ]
}
```

### Create Ad
```
POST /api/ads
```

**Request Body:**
```json
{
  "title": "string",
  "content": "string",
  "imageUrl": "string",
  "targetUrl": "string",
  "startDate": "2023-01-01T00:00:00Z",
  "endDate": "2023-01-31T00:00:00Z"
}
```

### Get Ad by ID
```
GET /api/ads/{id}
```

### Update Ad
```
PUT /api/ads/{id}
```

### Delete Ad
```
DELETE /api/ads/{id}
```

## Wallet Activities

### Get Transactions
```
GET /api/wallet/transactions
```

**Query Parameters:**
- `userId` (optional): Filter by user
- `type` (optional): Filter by transaction type
- `page` (optional): Page number
- `limit` (optional): Items per page

**Response:**
```json
{
  "transactions": [
    {
      "id": "string",
      "userId": "string",
      "type": "deposit|withdrawal|transfer",
      "amount": 1000,
      "currency": "USD",
      "status": "completed",
      "createdAt": "2023-01-15T00:00:00Z"
    }
  ]
}
```

### Get User Balances
```
GET /api/wallet/balances
```

**Response:**
```json
{
  "balances": [
    {
      "userId": "string",
      "currency": "USD",
      "amount": 1500
    }
  ]
}
```

## Social Tasks

### Get All Social Tasks
```
GET /api/social/tasks
```

### Create Social Task
```
POST /api/social/tasks
```

### Get Social Task by ID
```
GET /api/social/tasks/{id}
```

### Update Social Task
```
PUT /api/social/tasks/{id}
```

### Delete Social Task
```
DELETE /api/social/tasks/{id}
```

## User Management

### Get All Users
```
GET /api/users
```

**Query Parameters:**
- `page` (optional): Page number
- `limit` (optional): Items per page
- `search` (optional): Search term

**Response:**
```json
{
  "users": [
    {
      "id": "string",
      "name": "string",
      "email": "string",
      "role": "admin|user",
      "status": "active|suspended",
      "createdAt": "2023-01-15T00:00:00Z"
    }
  ]
}
```

### Get User by ID
```
GET /api/users/{id}
```

### Update User
```
PUT /api/users/{id}
```

### Delete User
```
DELETE /api/users/{id}
```

## Error Responses

All error responses follow this format:

```json
{
  "error": {
    "message": "string",
    "code": "string"
  }
}
```

### Common HTTP Status Codes

- `200`: Success
- `201`: Created
- `400`: Bad Request
- `401`: Unauthorized
- `403`: Forbidden
- `404`: Not Found
- `500`: Internal Server Error
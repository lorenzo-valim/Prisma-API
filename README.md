# Documentação Técnica — Prisma API

> **Versão do documento:** 1.0.0  
> **Última atualização:** 2025-05-13  
> **Stack:** C# .NET · MySQL · BCrypt · SMTP

---

## A. Visão Geral

A **Prisma API** é uma API RESTful responsável por gerir o ciclo completo de reservas de salas. Oferece funcionalidades de autenticação de utilizadores (registo, login, verificação de e-mail por OTP e redefinição de senha), gestão de salas, gestão de reservas com deteção de conflitos de horário e lista de espera automática (_waitlist_).

Todos os corpos de requisição e resposta utilizam o formato **JSON**. Campos de data e hora seguem o padrão **ISO-8601 (UTC)**.

### Base URL

| Ambiente       | URL Base                          |
|----------------|-----------------------------------|
| Desenvolvimento (local HTTP)  | `http://localhost:5263`           |
| Desenvolvimento (local HTTPS) | `https://localhost:7101`          |
| Homologação    | `https://api-hml.prisma.example.com` |
| Produção       | `https://api.prisma.example.com`  |

> **Nota CORS:** Em desenvolvimento, a API aceita requisições originadas de `http://localhost:5173` (servidor de desenvolvimento React/Vite).

---

## B. Dicionário de Status HTTP

| Código | Nome                  | Quando ocorre nesta API                                                                                  |
|--------|-----------------------|----------------------------------------------------------------------------------------------------------|
| `200`  | OK                    | Requisição bem-sucedida. Retornado em operações de leitura (GET) e em ações que retornam dados (POST de auth, DELETE de reserva). |
| `201`  | Created               | Não utilizado explicitamente; criações bem-sucedidas retornam `200 OK` com o objeto criado.              |
| `204`  | No Content            | Atualização bem-sucedida (PUT). Nenhum corpo é retornado.                                                |
| `400`  | Bad Request           | Dados inválidos ou ausentes no corpo da requisição (campos obrigatórios em falta, IDs inexistentes, data no passado, OTP inválido/expirado, e-mail já em uso). |
| `401`  | Unauthorized          | Credenciais de login incorretas (e-mail ou senha inválidos).                                             |
| `404`  | Not Found             | O recurso solicitado (sala, reserva ou utilizador) não foi encontrado pelo ID fornecido.                 |
| `500`  | Internal Server Error | Erro inesperado no servidor (ex.: hash de senha incompatível no banco de dados, falha no envio de e-mail). |
| `503`  | Service Unavailable   | Não foi possível contactar o servidor NTP para validar a data da reserva.                                |

---

## C. Tipos de Dados e Enumerações

### Enum `TipoUsuario`

| Valor | Representação numérica | Descrição        |
|-------|------------------------|------------------|
| `User`  | `1`                  | Utilizador comum |
| `Admin` | `2`                  | Administrador    |

> No JSON, este campo é serializado como **número inteiro** (`1` ou `2`).

### Enum `StatusReserva`

| Valor       | Representação numérica | Descrição          |
|-------------|------------------------|--------------------|
| `Ativa`     | `0`                    | Reserva ativa      |
| `Cancelada` | `1`                    | Reserva cancelada  |

> No JSON, este campo é serializado como **número inteiro** (`0` ou `1`).

---

## D. Documentação dos Endpoints

---

### 1. Autenticação (`/api/Auth`)

---

#### 1.1 Registar Utilizador

**Método e Rota**
```http
POST /api/Auth/register
```

**Descrição**  
Cria uma nova conta de utilizador. Após o registo bem-sucedido, um e-mail com um código OTP de 6 dígitos é enviado para o endereço informado. O utilizador deverá verificar o e-mail antes de poder utilizar a conta plenamente.

**Parâmetros**

| Nome       | Tipo          | Localização | Obrigatório | Descrição                              |
|------------|---------------|-------------|-------------|----------------------------------------|
| `nome`     | `String`      | Body        | Sim         | Nome completo do utilizador.           |
| `email`    | `String`      | Body        | Sim         | Endereço de e-mail (deve ser único).   |
| `password` | `String`      | Body        | Sim         | Senha em texto plano (será encriptada). |
| `tipo`     | `Integer`     | Body        | Sim         | Tipo de conta: `1` (User) ou `2` (Admin). |

**Exemplo de Requisição**
```json
{
  "nome": "Ana Silva",
  "email": "ana.silva@email.com",
  "password": "MinhaSenh@123",
  "tipo": 1
}
```

**Resposta de Sucesso — `200 OK`**
```json
"Usuário registrado com sucesso! Por favor, verifique sua caixa de entrada."
```

**Respostas de Erro**

`400 Bad Request` — E-mail já cadastrado:
```json
"O e-mail já está em uso."
```

`500 Internal Server Error` — Utilizador criado, mas falha no envio do e-mail:
```json
"Usuário criado, mas houve um erro ao enviar o email de verificação."
```

---

#### 1.2 Login

**Método e Rota**
```http
POST /api/Auth/login
```

**Descrição**  
Autentica um utilizador com e-mail e senha. Retorna os dados básicos do utilizador em caso de sucesso. Esta API não implementa JWT; o controlo de sessão deve ser gerido pelo cliente.

**Parâmetros**

| Nome       | Tipo     | Localização | Obrigatório | Descrição                    |
|------------|----------|-------------|-------------|------------------------------|
| `email`    | `String` | Body        | Sim         | E-mail do utilizador.        |
| `password` | `String` | Body        | Sim         | Senha em texto plano.        |

**Exemplo de Requisição**
```json
{
  "email": "ana.silva@email.com",
  "password": "MinhaSenh@123"
}
```

**Resposta de Sucesso — `200 OK`**
```json
{
  "message": "Bem-vindo, Ana Silva!",
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "email": "ana.silva@email.com"
}
```

**Respostas de Erro**

`400 Bad Request` — Campos em falta:
```json
{
  "message": "E-mail e senha são obrigatórios."
}
```

`401 Unauthorized` — Credenciais incorretas:
```json
{
  "message": "E-mail ou senha incorretos."
}
```

`500 Internal Server Error` — Senha no banco incompatível com BCrypt:
```json
{
  "message": "O formato da senha salva no banco é incompatível. Crie um novo usuário para testar."
}
```

---

#### 1.3 Verificar OTP (Verificação de E-mail)

**Método e Rota**
```http
POST /api/Auth/verify-otp
```

**Descrição**  
Confirma o código OTP de 6 dígitos enviado por e-mail no momento do registo. Após verificação bem-sucedida, o campo `isEmailVerified` do utilizador é definido como `true`.

**Parâmetros**

| Nome    | Tipo     | Localização | Obrigatório | Descrição                          |
|---------|----------|-------------|-------------|------------------------------------|
| `email` | `String` | Body        | Sim         | E-mail do utilizador a verificar.  |
| `code`  | `String` | Body        | Sim         | Código OTP de 6 dígitos recebido por e-mail. |

**Exemplo de Requisição**
```json
{
  "email": "ana.silva@email.com",
  "code": "482931"
}
```

**Resposta de Sucesso — `200 OK`**
```json
"Conta verificada com sucesso!"
```

**Respostas de Erro**

`400 Bad Request` — Utilizador não encontrado:
```json
"Usuário não encontrado."
```

`400 Bad Request` — Código OTP incorreto:
```json
"Código inválido."
```

`400 Bad Request` — Código OTP expirado:
```json
"O código expirou."
```

---

#### 1.4 Solicitar OTP de Redefinição de Senha

**Método e Rota**
```http
POST /api/Auth/send-otp-reset-password
```

**Descrição**  
Gera e envia um código OTP de 6 dígitos para o e-mail informado, permitindo que o utilizador inicie o processo de redefinição de senha. Por razões de segurança (prevenção de enumeração de utilizadores), a resposta de sucesso é idêntica mesmo que o e-mail não exista no sistema.

**Parâmetros**

| Nome    | Tipo     | Localização | Obrigatório | Descrição                     |
|---------|----------|-------------|-------------|-------------------------------|
| `email` | `String` | Body        | Sim         | E-mail da conta a recuperar.  |

> **Nota:** Os campos `code` e `newPassword` são ignorados neste endpoint, mesmo que enviados.

**Exemplo de Requisição**
```json
{
  "email": "ana.silva@email.com"
}
```

**Resposta de Sucesso — `200 OK`**
```json
"Se o e-mail existir, um código de redefinição será enviado."
```

**Respostas de Erro**

`400 Bad Request` — E-mail não existe (retorna mensagem genérica por segurança):
```json
"Se o e-mail existir, um código de redefinição será enviado."
```

`500 Internal Server Error` — Falha no envio do e-mail:
```json
"Houve um erro ao enviar o email de redefinição."
```

---

#### 1.5 Redefinir Senha

**Método e Rota**
```http
POST /api/Auth/reset-password
```

**Descrição**  
Valida o código OTP de redefinição e, caso seja válido e não esteja expirado, atualiza a senha do utilizador. Após uso, o código OTP é invalidado e não pode ser reutilizado.

**Parâmetros**

| Nome          | Tipo     | Localização | Obrigatório | Descrição                                     |
|---------------|----------|-------------|-------------|-----------------------------------------------|
| `email`       | `String` | Body        | Sim         | E-mail da conta a redefinir.                  |
| `code`        | `String` | Body        | Sim         | Código OTP de 6 dígitos recebido por e-mail.  |
| `newPassword` | `String` | Body        | Sim         | Nova senha em texto plano.                    |

**Exemplo de Requisição**
```json
{
  "email": "ana.silva@email.com",
  "code": "739201",
  "newPassword": "NovaSenha@456"
}
```

**Resposta de Sucesso — `200 OK`**
```json
"Senha atualizada com sucesso!"
```

**Respostas de Erro**

`400 Bad Request` — Dados inválidos ou código incorreto:
```json
"Dados inválidos ou código incorreto."
```

`400 Bad Request` — Código expirado:
```json
"O código expirou. Por favor, solicite um novo."
```

---

### 2. Utilizadores (`/api/Usuarios`)

---

#### 2.1 Listar Todos os Utilizadores

**Método e Rota**
```http
GET /api/Usuarios
```

**Descrição**  
Retorna a lista completa de utilizadores registados. O campo `passwordHash` é omitido da resposta por segurança.

**Parâmetros**  
Nenhum.

**Resposta de Sucesso — `200 OK`**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "nome": "Ana Silva",
    "email": "ana.silva@email.com",
    "tipo": 1,
    "criacao": "2025-05-10T14:30:00Z",
    "isEmailVerified": true,
    "verificationToken": null,
    "verificationTokenResetPassword": null,
    "tokenExpiration": null,
    "tokenExpirationResetPassword": null
  },
  {
    "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "nome": "Carlos Admin",
    "email": "carlos.admin@email.com",
    "tipo": 2,
    "criacao": "2025-04-01T09:00:00Z",
    "isEmailVerified": true,
    "verificationToken": null,
    "verificationTokenResetPassword": null,
    "tokenExpiration": null,
    "tokenExpirationResetPassword": null
  }
]
```

---

#### 2.2 Obter Utilizador por ID

**Método e Rota**
```http
GET /api/Usuarios/{id}
```

**Descrição**  
Retorna os dados de um utilizador específico pelo seu UUID.

**Parâmetros**

| Nome | Tipo     | Localização | Obrigatório | Descrição                    |
|------|----------|-------------|-------------|------------------------------|
| `id` | `String (UUID)` | Path   | Sim         | UUID do utilizador.          |

**Resposta de Sucesso — `200 OK`**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "nome": "Ana Silva",
  "email": "ana.silva@email.com",
  "tipo": 1,
  "criacao": "2025-05-10T14:30:00Z",
  "isEmailVerified": true,
  "verificationToken": null,
  "verificationTokenResetPassword": null,
  "tokenExpiration": null,
  "tokenExpirationResetPassword": null
}
```

**Respostas de Erro**

`404 Not Found` — Utilizador não encontrado:
```json
{
  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.5",
  "title": "Not Found",
  "status": 404
}
```

---

#### 2.3 Atualizar Utilizador

**Método e Rota**
```http
PUT /api/Usuarios/{id}
```

**Descrição**  
Atualiza todos os campos de um utilizador existente. O `id` no Path deve ser idêntico ao `id` no corpo da requisição.

**Parâmetros**

| Nome                            | Tipo            | Localização | Obrigatório | Descrição                                        |
|---------------------------------|-----------------|-------------|-------------|--------------------------------------------------|
| `id`                            | `String (UUID)` | Path        | Sim         | UUID do utilizador a atualizar.                  |
| `id`                            | `String (UUID)` | Body        | Sim         | Deve ser idêntico ao ID no Path.                 |
| `nome`                          | `String`        | Body        | Sim         | Nome completo do utilizador.                     |
| `email`                         | `String`        | Body        | Sim         | Endereço de e-mail.                              |
| `tipo`                          | `Integer`       | Body        | Sim         | Tipo de conta: `1` (User) ou `2` (Admin).        |
| `criacao`                       | `String (ISO-8601)` | Body    | Sim         | Data de criação original (não alterar).          |
| `isEmailVerified`               | `Boolean`       | Body        | Sim         | Estado de verificação do e-mail.                 |
| `verificationToken`             | `String\|null`  | Body        | Não         | Token OTP ativo (normalmente `null`).            |
| `verificationTokenResetPassword`| `String\|null`  | Body        | Não         | Token OTP de reset ativo (normalmente `null`).   |
| `tokenExpiration`               | `String (ISO-8601)\|null` | Body | Não    | Data de expiração do token de verificação.       |
| `tokenExpirationResetPassword`  | `String (ISO-8601)\|null` | Body | Não    | Data de expiração do token de reset de senha.    |

**Exemplo de Requisição**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "nome": "Ana Silva Atualizado",
  "email": "ana.nova@email.com",
  "tipo": 1,
  "criacao": "2025-05-10T14:30:00Z",
  "isEmailVerified": true,
  "verificationToken": null,
  "verificationTokenResetPassword": null,
  "tokenExpiration": null,
  "tokenExpirationResetPassword": null
}
```

**Resposta de Sucesso — `204 No Content`**  
Sem corpo de resposta.

**Respostas de Erro**

`400 Bad Request` — ID no Path diferente do ID no Body:
```json
"O ID do usuário não pode ser modificado."
```

`404 Not Found` — Utilizador não encontrado (detetado por concorrência):
```json
{
  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.5",
  "title": "Not Found",
  "status": 404
}
```

---

#### 2.4 Eliminar Utilizador

**Método e Rota**
```http
DELETE /api/Usuarios/{id}
```

**Descrição**  
Remove permanentemente um utilizador do sistema. Retorna os dados do utilizador eliminado.

**Parâmetros**

| Nome | Tipo            | Localização | Obrigatório | Descrição                    |
|------|-----------------|-------------|-------------|------------------------------|
| `id` | `String (UUID)` | Path        | Sim         | UUID do utilizador a eliminar. |

**Resposta de Sucesso — `200 OK`**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "nome": "Ana Silva",
  "email": "ana.silva@email.com",
  "tipo": 1,
  "criacao": "2025-05-10T14:30:00Z",
  "isEmailVerified": true,
  "verificationToken": null,
  "verificationTokenResetPassword": null,
  "tokenExpiration": null,
  "tokenExpirationResetPassword": null
}
```

**Respostas de Erro**

`404 Not Found` — Utilizador não encontrado:
```json
{
  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.5",
  "title": "Not Found",
  "status": 404
}
```

---

### 3. Salas (`/api/Salas`)

---

#### 3.1 Criar Sala

**Método e Rota**
```http
POST /api/Salas
```

**Descrição**  
Cria uma nova sala no sistema. O `id` é gerado automaticamente pelo servidor (UUID v4).

**Parâmetros**

| Nome             | Tipo      | Localização | Obrigatório | Descrição                                |
|------------------|-----------|-------------|-------------|------------------------------------------|
| `nome`           | `String`  | Body        | Sim         | Nome identificador da sala.              |
| `capacidade`     | `Integer` | Body        | Sim         | Número máximo de pessoas da sala.        |
| `disponibilidade`| `Boolean` | Body        | Sim         | `true` se a sala está disponível para reservas. |

**Exemplo de Requisição**
```json
{
  "nome": "Sala Inovação",
  "capacidade": 20,
  "disponibilidade": true
}
```

**Resposta de Sucesso — `200 OK`**
```json
{
  "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "nome": "Sala Inovação",
  "capacidade": 20,
  "disponibilidade": true
}
```

---

#### 3.2 Listar Todas as Salas

**Método e Rota**
```http
GET /api/Salas
```

**Descrição**  
Retorna a lista de todas as salas cadastradas.

**Parâmetros**  
Nenhum.

**Resposta de Sucesso — `200 OK`**
```json
[
  {
    "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "nome": "Sala Inovação",
    "capacidade": 20,
    "disponibilidade": true
  },
  {
    "id": "d4e5f6a7-b8c9-0123-defa-234567890123",
    "nome": "Sala Reunião A",
    "capacidade": 8,
    "disponibilidade": false
  }
]
```

---

#### 3.3 Obter Sala por ID

**Método e Rota**
```http
GET /api/Salas/{id}
```

**Descrição**  
Retorna os dados de uma sala específica pelo seu UUID.

**Parâmetros**

| Nome | Tipo            | Localização | Obrigatório | Descrição           |
|------|-----------------|-------------|-------------|---------------------|
| `id` | `String (UUID)` | Path        | Sim         | UUID da sala.       |

**Resposta de Sucesso — `200 OK`**
```json
{
  "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "nome": "Sala Inovação",
  "capacidade": 20,
  "disponibilidade": true
}
```

**Respostas de Erro**

`404 Not Found` — Sala não encontrada:
```json
{
  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.5",
  "title": "Not Found",
  "status": 404
}
```

---

#### 3.4 Atualizar Sala

**Método e Rota**
```http
PUT /api/Salas/{id}
```

**Descrição**  
Atualiza todos os campos de uma sala existente. O `id` no Path deve ser idêntico ao `id` no corpo da requisição.

**Parâmetros**

| Nome              | Tipo            | Localização | Obrigatório | Descrição                                        |
|-------------------|-----------------|-------------|-------------|--------------------------------------------------|
| `id`              | `String (UUID)` | Path        | Sim         | UUID da sala a atualizar.                        |
| `id`              | `String (UUID)` | Body        | Sim         | Deve ser idêntico ao ID no Path.                 |
| `nome`            | `String`        | Body        | Sim         | Nome identificador da sala.                      |
| `capacidade`      | `Integer`       | Body        | Sim         | Número máximo de pessoas.                        |
| `disponibilidade` | `Boolean`       | Body        | Sim         | `true` se disponível para reservas.              |

**Exemplo de Requisição**
```json
{
  "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "nome": "Sala Inovação Premium",
  "capacidade": 25,
  "disponibilidade": true
}
```

**Resposta de Sucesso — `204 No Content`**  
Sem corpo de resposta.

**Respostas de Erro**

`400 Bad Request` — ID no Path diferente do ID no Body:
```json
"O ID da sala não pode ser modificado."
```

---

#### 3.5 Eliminar Sala

**Método e Rota**
```http
DELETE /api/Salas/{id}
```

**Descrição**  
Remove permanentemente uma sala do sistema. Retorna os dados da sala eliminada.

**Parâmetros**

| Nome | Tipo            | Localização | Obrigatório | Descrição                    |
|------|-----------------|-------------|-------------|------------------------------|
| `id` | `String (UUID)` | Path        | Sim         | UUID da sala a eliminar.     |

**Resposta de Sucesso — `200 OK`**
```json
{
  "id": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "nome": "Sala Inovação",
  "capacidade": 20,
  "disponibilidade": true
}
```

**Respostas de Erro**

`404 Not Found` — Sala não encontrada:
```json
{
  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.5",
  "title": "Not Found",
  "status": 404
}
```

---

### 4. Reservas (`/api/Reserva`)

> **Comportamento de conflito:** Ao criar ou atualizar uma reserva, a API valida automaticamente a data utilizando o servidor NTP oficial (`a.st1.ntp.br`). Se houver conflito de horário com outra reserva ativa na mesma sala, o utilizador é adicionado à **lista de espera (_waitlist_)** automaticamente. Ao cancelar uma reserva, o primeiro utilizador da _waitlist_ para aquele slot é promovido automaticamente a uma nova reserva ativa.

---

#### 4.1 Criar Reserva

**Método e Rota**
```http
POST /api/Reserva
```

**Descrição**  
Cria uma nova reserva de sala. Se houver conflito de horário com uma reserva ativa, o pedido é automaticamente inserido na lista de espera.

**Parâmetros**

| Nome             | Tipo            | Localização | Obrigatório | Descrição                                                        |
|------------------|-----------------|-------------|-------------|------------------------------------------------------------------|
| `usuarioId`      | `String (UUID)` | Body        | Sim         | UUID do utilizador que está a fazer a reserva.                   |
| `salaId`         | `String (UUID)` | Body        | Sim         | UUID da sala a reservar.                                         |
| `dataReserva`    | `String (ISO-8601)` | Body    | Sim         | Data da reserva (não pode ser no passado). Ex.: `"2025-06-15T00:00:00Z"` |
| `horarioInicio`  | `String (HH:mm:ss)` | Body    | Sim         | Horário de início do período reservado. Ex.: `"09:00:00"`        |
| `horarioFim`     | `String (HH:mm:ss)` | Body    | Sim         | Horário de fim do período reservado. Ex.: `"11:00:00"`           |

**Exemplo de Requisição**
```json
{
  "usuarioId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "salaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "dataReserva": "2025-06-15T00:00:00Z",
  "horarioInicio": "09:00:00",
  "horarioFim": "11:00:00"
}
```

**Resposta de Sucesso — `200 OK` (Reserva criada)**
```json
{
  "id": "e5f6a7b8-c9d0-1234-efab-345678901234",
  "usuarioId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "salaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "dataReserva": "2025-06-15T00:00:00Z",
  "horarioInicio": "09:00:00",
  "horarioFim": "11:00:00",
  "status": 0
}
```

**Resposta de Sucesso — `200 OK` (Conflito — adicionado à waitlist)**
```json
{
  "message": "Horário ocupado. Adicionado à lista de espera.",
  "waitlistId": 7
}
```

**Respostas de Erro**

`400 Bad Request` — IDs inválidos (sala ou utilizador não existem):
```json
"SalaId ou UsuarioId inválido."
```

`400 Bad Request` — Data da reserva no passado:
```json
"A data da reserva não pode ser no passado."
```

`503 Service Unavailable` — Falha ao contactar servidor NTP:
```json
"Não foi possível validar o horário oficial."
```

---

#### 4.2 Listar Todas as Reservas

**Método e Rota**
```http
GET /api/Reserva
```

**Descrição**  
Retorna a lista de todas as reservas cadastradas, independentemente do status.

**Parâmetros**  
Nenhum.

**Resposta de Sucesso — `200 OK`**
```json
[
  {
    "id": "e5f6a7b8-c9d0-1234-efab-345678901234",
    "usuarioId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "salaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "dataReserva": "2025-06-15T00:00:00Z",
    "horarioInicio": "09:00:00",
    "horarioFim": "11:00:00",
    "status": 0
  },
  {
    "id": "f6a7b8c9-d0e1-2345-fabc-456789012345",
    "usuarioId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "salaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "dataReserva": "2025-06-15T00:00:00Z",
    "horarioInicio": "14:00:00",
    "horarioFim": "16:00:00",
    "status": 1
  }
]
```

---

#### 4.3 Obter Reserva por ID

**Método e Rota**
```http
GET /api/Reserva/{id}
```

**Descrição**  
Retorna os dados de uma reserva específica pelo seu ID numérico.

**Parâmetros**

| Nome | Tipo            | Localização | Obrigatório | Descrição               |
|------|-----------------|-------------|-------------|-------------------------|
| `id` | `String (UUID)` | Path        | Sim         | UUID da reserva.        |

**Resposta de Sucesso — `200 OK`**
```json
{
  "id": "e5f6a7b8-c9d0-1234-efab-345678901234",
  "usuarioId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "salaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "dataReserva": "2025-06-15T00:00:00Z",
  "horarioInicio": "09:00:00",
  "horarioFim": "11:00:00",
  "status": 0
}
```

**Respostas de Erro**

`404 Not Found` — Reserva não encontrada:
```json
{
  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.5",
  "title": "Not Found",
  "status": 404
}
```

---

#### 4.4 Atualizar Reserva

**Método e Rota**
```http
PUT /api/Reserva/{id}
```

**Descrição**  
Atualiza os dados de uma reserva existente. Aplica as mesmas validações de data (NTP) e de IDs que o endpoint de criação. O `id` no Path deve ser idêntico ao `id` no corpo da requisição.

**Parâmetros**

| Nome             | Tipo            | Localização | Obrigatório | Descrição                                                          |
|------------------|-----------------|-------------|-------------|--------------------------------------------------------------------|
| `id`             | `String (UUID)` | Path        | Sim         | UUID da reserva a atualizar.                                       |
| `id`             | `String (UUID)` | Body        | Sim         | Deve ser idêntico ao ID no Path.                                   |
| `usuarioId`      | `String (UUID)` | Body        | Sim         | UUID do utilizador responsável pela reserva.                       |
| `salaId`         | `String (UUID)` | Body        | Sim         | UUID da sala reservada.                                            |
| `dataReserva`    | `String (ISO-8601)` | Body    | Sim         | Data da reserva (não pode ser no passado).                         |
| `horarioInicio`  | `String (HH:mm:ss)` | Body    | Sim         | Horário de início.                                                 |
| `horarioFim`     | `String (HH:mm:ss)` | Body    | Sim         | Horário de fim.                                                    |
| `status`         | `Integer`       | Body        | Sim         | Status da reserva: `0` (Ativa) ou `1` (Cancelada).                |

**Exemplo de Requisição**
```json
{
  "id": "e5f6a7b8-c9d0-1234-efab-345678901234",
  "usuarioId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "salaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "dataReserva": "2025-06-20T00:00:00Z",
  "horarioInicio": "10:00:00",
  "horarioFim": "12:00:00",
  "status": 0
}
```

**Resposta de Sucesso — `204 No Content`**  
Sem corpo de resposta.

**Respostas de Erro**

`400 Bad Request` — ID no Path diferente do ID no Body:
```json
"O ID da reserva não pode ser modificado."
```

`400 Bad Request` — IDs de sala ou utilizador inválidos:
```json
"SalaId ou UsuarioId inválido."
```

`400 Bad Request` — Data da reserva no passado:
```json
"A data da reserva não pode ser no passado."
```

`503 Service Unavailable` — Falha ao contactar servidor NTP:
```json
"Não foi possível validar o horário oficial."
```

---

#### 4.5 Cancelar Reserva

**Método e Rota**
```http
DELETE /api/Reserva/{id}
```

**Descrição**  
Cancela uma reserva, definindo o seu `status` para `1` (Cancelada). **Não remove** o registo do banco de dados. Após o cancelamento, o sistema verifica automaticamente a _waitlist_ e, se existir um candidato para o mesmo slot (sala + data + horário), uma nova reserva ativa é criada para esse utilizador.

**Parâmetros**

| Nome | Tipo            | Localização | Obrigatório | Descrição                          |
|------|-----------------|-------------|-------------|------------------------------------|
| `id` | `String (UUID)` | Path        | Sim         | UUID da reserva a cancelar.        |

**Resposta de Sucesso — `200 OK`**  
Retorna a reserva com o status atualizado para `1` (Cancelada):
```json
{
  "id": "e5f6a7b8-c9d0-1234-efab-345678901234",
  "usuarioId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "salaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
  "dataReserva": "2025-06-15T00:00:00Z",
  "horarioInicio": "09:00:00",
  "horarioFim": "11:00:00",
  "status": 1
}
```

**Respostas de Erro**

`404 Not Found` — Reserva não encontrada:
```json
{
  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.5",
  "title": "Not Found",
  "status": 404
}
```

---

#### 4.6 Relatório de Reservas por Período

**Método e Rota**
```http
GET /api/Reserva/relatorio?dataInicio={dataInicio}&dataFim={dataFim}
```

**Descrição**  
Retorna todas as reservas cujo campo `dataReserva` esteja dentro do intervalo definido pelos parâmetros de query (inclusivo em ambas as extremidades).

**Parâmetros**

| Nome          | Tipo                | Localização | Obrigatório | Descrição                                         |
|---------------|---------------------|-------------|-------------|---------------------------------------------------|
| `dataInicio`  | `String (ISO-8601)` | Query       | Sim         | Data de início do período. Ex.: `2025-01-01T00:00:00Z` |
| `dataFim`     | `String (ISO-8601)` | Query       | Sim         | Data de fim do período. Ex.: `2025-12-31T23:59:59Z` |

**Exemplo de Requisição**
```http
GET /api/Reserva/relatorio?dataInicio=2025-01-01T00:00:00Z&dataFim=2025-12-31T23:59:59Z
```

**Resposta de Sucesso — `200 OK`**
```json
[
  {
    "id": "e5f6a7b8-c9d0-1234-efab-345678901234",
    "usuarioId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "salaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "dataReserva": "2025-06-15T00:00:00Z",
    "horarioInicio": "09:00:00",
    "horarioFim": "11:00:00",
    "status": 0
  },
  {
    "id": "f6a7b8c9-d0e1-2345-fabc-456789012345",
    "usuarioId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "salaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "dataReserva": "2025-08-20T00:00:00Z",
    "horarioInicio": "14:00:00",
    "horarioFim": "16:00:00",
    "status": 1
  }
]
```

> Retorna uma lista vazia (`[]`) se não houver reservas no período informado.

---

#### 4.7 Listar Waitlist

**Método e Rota**
```http
GET /api/Reserva/waitlist
```

**Descrição**  
Retorna todos os registos da lista de espera, ordenados por data de solicitação (do mais antigo para o mais recente), refletindo a ordem de prioridade para promoção.

**Parâmetros**  
Nenhum.

**Resposta de Sucesso — `200 OK`**
```json
[
  {
    "id": "aa11bb22-cc33-dd44-ee55-ff6677889900",
    "usuarioId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "salaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "dataReserva": "2025-06-15T00:00:00Z",
    "horarioInicio": "09:00:00",
    "horarioFim": "11:00:00",
    "dataSolicitacao": "2025-05-12T10:45:00Z"
  },
  {
    "id": "bb22cc33-dd44-ee55-ff66-001122334455",
    "usuarioId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "salaId": "c3d4e5f6-a7b8-9012-cdef-123456789012",
    "dataReserva": "2025-06-15T00:00:00Z",
    "horarioInicio": "09:00:00",
    "horarioFim": "11:00:00",
    "dataSolicitacao": "2025-05-13T08:30:00Z"
  }
]
```

> Retorna uma lista vazia (`[]`) se a waitlist estiver vazia.

---

## E. Notas de Implementação para Clientes

### Sobre Autenticação e Sessão

A API **não emite tokens JWT** nem cookies de sessão. Após um login bem-sucedido, o cliente (App Kotlin ou Site React) é responsável por armazenar localmente o `id` e o `email` retornados e gerir o estado de autenticação da sessão.

### Sobre Formatos de Data e Hora

| Tipo C#        | Formato esperado no JSON         | Exemplo                        |
|----------------|----------------------------------|--------------------------------|
| `DateTime`     | String ISO-8601 (UTC, sufixo `Z`) | `"2025-06-15T09:00:00Z"`       |
| `DateTime?`    | String ISO-8601 ou `null`        | `"2025-06-15T09:00:00Z"` / `null` |
| `TimeSpan`     | String `HH:mm:ss`                | `"09:00:00"`                   |

### Sobre Tipos de ID

Todos os recursos utilizam **UUID v4** como identificador, sem exceção.

| Recurso    | Tipo de ID         | Exemplo                                    |
|------------|--------------------|--------------------------------------------|
| `Usuario`  | `String (UUID v4)` | `"a1b2c3d4-e5f6-7890-abcd-ef1234567890"`   |
| `Sala`     | `String (UUID v4)` | `"c3d4e5f6-a7b8-9012-cdef-123456789012"`   |
| `Reserva`  | `String (UUID v4)` | `"e5f6a7b8-c9d0-1234-efab-345678901234"`   |
| `Waitlist` | `String (UUID v4)` | `"aa11bb22-cc33-dd44-ee55-ff6677889900"`   |

### Sobre o Header `Content-Type`

Todas as requisições com corpo (POST, PUT) devem incluir o header:

```http
Content-Type: application/json
```

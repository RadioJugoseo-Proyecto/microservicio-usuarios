
# Radio Jugoseo - Microservicio de Usuarios

Microservicio del proyecto Radio Jugoseo, desarrollado con
Spring Boot y MySQL.

## Responsabilidades

Este microservicio administra:

- Registro de usuarios.
- Roles: Cliente, Locutor y Administrador.
- Almacenamiento seguro de contraseñas con BCrypt.
- Validación de datos de registro, incluyendo RUN chileno.
- Persistencia de usuarios y roles en MySQL.

## Tecnologías

- Java 17
- Spring Boot 4.1.2
- Spring Security
- Spring Data JPA
- MySQL
- Maven

## Base de datos

Este microservicio utiliza una base de datos independiente:

`radiojugoseo_usuarios`

El archivo `database/schema.sql` permite crear las tablas
necesarias e insertar los roles iniciales.

Para configurar la base de datos:

1. Tener MySQL instalado y en ejecución.
2. Ejecutar `database/schema.sql` desde MySQL Workbench
   utilizando una cuenta con permisos para crear bases de datos.
3. Configurar un usuario MySQL llamado `jugoseo_app` con
   permisos sobre `radiojugoseo_usuarios`.
4. Configurar la variable de entorno `DB_PASSWORD`
   con la contraseña de ese usuario MySQL.

El script no crea usuarios de MySQL ni configura sus permisos.

## Ejecutar el proyecto

Desde la raíz del repositorio, configurar `DB_PASSWORD`
en PowerShell:

```powershell
$segura = Read-Host "Contraseña de jugoseo_app" -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new("", $segura).Password
```

Después ejecutar:

```powershell
.\mvnw.cmd spring-boot:run
```

El microservicio se ejecuta en:

`http://localhost:8081`

## API disponible

### Registrar cliente

`POST /api/v1/auth/registro`

Permite registrar un nuevo usuario con el rol Cliente.

La contraseña se almacena cifrada mediante BCrypt.

Respuestas principales:

- `201 Created`: usuario registrado.
- `400 Bad Request`: datos inválidos.
- `409 Conflict`: correo o RUN duplicado.

## Estado del desarrollo

Implementado:
- Registro de clientes.
- Validación de campos obligatorios.
- Validación de RUN chileno.
- Cifrado de contraseñas.
- Manejo inicial de errores.

Pendiente:
- Inicio de sesión.
- Autenticación JWT.
- Autorización por roles.
- Validaciones adicionales.
- Pruebas automatizadas.

## Arquitectura

Este repositorio contiene exclusivamente el microservicio
de usuarios.

Los microservicios de tienda y programas se desarrollan
en repositorios independientes dentro de RadioJugoseo-Proyecto.
```

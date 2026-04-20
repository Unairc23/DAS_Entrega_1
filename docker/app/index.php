<?php

// ---------- Configuración de la base de datos ----------
define('DB_HOST',    'db');
define('DB_USER',    'admin');
define('DB_PASS',    'test');
define('DB_NAME',    'database');
define('DB_CHARSET', 'utf8mb4');

// ---------- Cabeceras ----------
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// ---------- Leer el body JSON ---------- 
$rawBody = file_get_contents('php://input');
$data    = [];
if (in_array($_SERVER['REQUEST_METHOD'], ['POST', 'PUT'])) {
    $data = json_decode($rawBody, true) ?? [];
    if (json_last_error() !== JSON_ERROR_NONE) {
        sendResponse(400, 'error', 'Body JSON inválido');
    }
}

// ---------- Conexión ----------
$pdo = connectDB();

// ---------- Enrutador ----------
switch (strtolower(trim($data['accion']))) {
 
    case 'add':
        addActividad($pdo, $data);
        break;
 
    case 'update':
        updateActividad($pdo, $data);
        break;
 
    case 'getall':
        getActividades($pdo, $data);
        break;
 
    case 'getbyid':
        getActividadPorId($pdo, $data);
        break;
 
    case 'delete':
        deleteActividadPorId($pdo, $data);
        break;
    
    case 'login':
        loginUsuario($pdo, $data);
        break;
    
    case 'register':
        registrarUsuario($pdo, $data);
        break;

    case 'getuser':
        getUsuario($pdo, $data);
        break;

    case 'cambiarimagen':
        cambiarImagen($pdo, $data);
        break;
 
    default:
        sendResponse(400, 'error', "Acción desconocida");
}

function addActividad(PDO $pdo, array $data): void
{
    validateFields($data, ['nombre', 'latitud', 'longitud', 'distancia', 'duracion', 'userId']);

    $stmt = $pdo->prepare(
        'INSERT INTO Actividades (Nombre, Latitud, Longitud, Descripcion, Distancia, Duracion, UserId)
         VALUES (:nombre, :latitud, :longitud, :descripcion, :distancia, :duracion, :userId)'
    );
    $stmt->execute([
        ':nombre'      => $data['nombre'],
        ':latitud'     => (float) $data['latitud'],
        ':longitud'    => (float) $data['longitud'],
        ':descripcion' => $data['descripcion'] ?? null,
        ':distancia'   => (float) $data['distancia'],
        ':duracion'    => (float) $data['duracion'],
        ':userId'       => (int) $data['userId'],
    ]);

    sendResponse(201, 'success', 'Actividad creada correctamente.', ['id' => (int)$pdo->lastInsertId()]);
}

function updateActividad(PDO $pdo, array $data): void
{
    validateFields($data, ['id', 'nombre', 'latitud', 'longitud', 'distancia', 'duracion']);

    $stmt = $pdo->prepare(
        'UPDATE Actividades
         SET Nombre=:nombre, Latitud=:latitud, Longitud=:longitud,
             Descripcion=:descripcion, Distancia=:distancia, Duracion=:duracion
         WHERE Id=:id'
    );
    $stmt->execute([
        ':id'          => (int)   $data['id'],
        ':nombre'      =>         $data['nombre'],
        ':latitud'     => (float) $data['latitud'],
        ':longitud'    => (float) $data['longitud'],
        ':descripcion' =>         $data['descripcion'] ?? null,
        ':distancia'   => (float) $data['distancia'],
        ':duracion'    => (float) $data['duracion'],
    ]);

    if ($stmt->rowCount() === 0) {
        sendResponse(404, 'error', "No se encontró ninguna actividad con Id={$data['id']}.");
    }
    sendResponse(200, 'success', 'Actividad actualizada correctamente.');
}

function getActividades(PDO $pdo, array $data): void
{
    validateFields($data, ['userId']);
    $stmt = $pdo->prepare(
        'SELECT Id, Nombre, Latitud, Longitud, Descripcion, Distancia, Duracion FROM Actividades WHERE UserId = :userId'
    );
    $stmt->execute([':userId' => (int) $data['userId']]);
    $actividades = $stmt->fetchAll(PDO::FETCH_ASSOC);

    foreach ($actividades as &$a) {
        $a['Id']        = (int)   $a['Id'];
        $a['Latitud']   = (float) $a['Latitud'];
        $a['Longitud']  = (float) $a['Longitud'];
        $a['Distancia'] = (float) $a['Distancia'];
        $a['Duracion']  = (float) $a['Duracion'];
    }
    sendResponse(200, 'success', null, ['actividades' => $actividades]);
}

function getActividadPorId(PDO $pdo, array $data): void
{
    validateFields($data, ['id']);

    $stmt = $pdo->prepare(
        'SELECT Id, Nombre, Latitud, Longitud, Descripcion, Distancia, Duracion
         FROM Actividades WHERE Id = :id'
    );
    $stmt->execute([':id' => (int)$data['id']]);
    $actividad = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$actividad) {
        sendResponse(404, 'error', "No se encontró ninguna actividad con Id={$data['id']}.");
    }

    $actividad['Id']        = (int)   $actividad['Id'];
    $actividad['Latitud']   = (float) $actividad['Latitud'];
    $actividad['Longitud']  = (float) $actividad['Longitud'];
    $actividad['Distancia'] = (float) $actividad['Distancia'];
    $actividad['Duracion']  = (float) $actividad['Duracion'];

    sendResponse(200, 'success', null, ['actividad' => $actividad]);
}

function deleteActividadPorId(PDO $pdo, array $data): void
{
    validateFields($data, ['id']);

    $stmt = $pdo->prepare('DELETE FROM Actividades WHERE Id = :id');
    $stmt->execute([':id' => (int)$data['id']]);

    if ($stmt->rowCount() === 0) {
        sendResponse(404, 'error', "No se encontró ninguna actividad con Id={$data['id']}.");
    }
    sendResponse(200, 'success', 'Actividad eliminada correctamente.');
}

function loginUsuario(PDO $pdo, array $data): void
{
    $stmt = $pdo->prepare('SELECT Id FROM Usuarios WHERE Nombre = :nombre AND Contraseña = :contrasena');
    $stmt->execute([
        ':nombre' => $data['nombre'],
        ':contrasena' => $data['contraseña']
    ]);
    if ($stmt->rowCount() === 0) {
        sendResponse(404, 'error', "Usuario o contraseña incorrectas");
    }
    $usuario = $stmt->fetch(PDO::FETCH_ASSOC);
    sendResponse(200, 'success', 'Usuario y contraseña correctos.', ['id' => (int)$usuario['Id']]);
}

function registrarUsuario(PDO $pdo, array $data): void
{
    $stmt = $pdo->prepare('SELECT Id FROM Usuarios WHERE Nombre = :nombre');
    $stmt->execute([':nombre' => $data['nombre']]);
    if ($stmt->rowCount() === 0) {
        $rstmt = $pdo->prepare(
            'INSERT INTO Usuarios (Nombre, Contraseña)
            VALUES (:nombre, :contrasena)'
        );
        $rstmt->execute([
            ':nombre'       => $data['nombre'],
            ':contrasena'   => $data['contraseña'],
        ]);
        sendResponse(201, 'success', 'Usuario creado correctamente.', ['id' => (int)$pdo->lastInsertId()]);
    }
    else {
        sendResponse(404, 'error', "Ya existe este usuario");
    }
}

function cambiarImagen(PDO $pdo, array $data): void
{
    validateFields($data, ['id', 'imagen']);

    $userId  = (int) $data['id'];
    $carpeta = "/var/www/html/uploads";

    if (!is_dir($carpeta)) {
        mkdir($carpeta, 0755, true);
    }

    // Decodificar Base64
    $imagenData = base64_decode($data['imagen']);
    if ($imagenData === false) {
        sendResponse(400, 'error', 'Imagen Base64 inválida');
    }

    $rutaArchivo = "{$carpeta}/Perfil{$userId}.jpg";
    if (file_put_contents($rutaArchivo, $imagenData) === false) {
        sendResponse(500, 'error', 'No se pudo guardar la imagen');
    }

    // Guardar la ruta en la DB
    $stmt = $pdo->prepare('UPDATE Usuarios SET Perfil = :perfil WHERE Id = :id');
    $stmt->execute([
        ':perfil' => "uploads/Perfil{$userId}.jpg",
        ':id'     => $userId,
    ]);

    sendResponse(200, 'success', 'Imagen actualizada correctamente.', [
        'ruta' => "uploads/Perfil{$userId}.jpg"
    ]);
}

function getUsuario(PDO $pdo, array $data): void
{
    validateFields($data, ['id']);

    $stmt = $pdo->prepare(
        'SELECT Id, Nombre, Perfil FROM Usuarios WHERE Id = :id'
    );
    $stmt->execute([':id' => (int) $data['id']]);
    $usuario = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$usuario) {
        sendResponse(404, 'error', "No se encontró ningún usuario con Id={$data['id']}.");
    }

    $usuario['Id'] = (int) $usuario['Id'];

    if ($usuario['Perfil'] !== null) {
        $rutaCompleta = "/var/www/html/" . $usuario['Perfil'];
        if (file_exists($rutaCompleta)) {
            $usuario['imagen'] = base64_encode(file_get_contents($rutaCompleta));
        } else {
            $usuario['imagen'] = null;
        }
    } else {
        $usuario['imagen'] = null;
    }

    sendResponse(200, 'success', null, ['usuario' => $usuario]);
}

function connectDB(): PDO
{
    $dsn     = 'mysql:host=' . DB_HOST . ';dbname=' . DB_NAME . ';charset=' . DB_CHARSET;
    $options = [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES   => false,
    ];
    try {
        return new PDO($dsn, DB_USER, DB_PASS, $options);
    } catch (\PDOException $e) {
        sendResponse(500, 'error', 'No se pudo conectar a la base de datos: ' . $e->getMessage());
    }
}

function validateFields(array $data, array $required): void
{
    $missing = [];
    foreach ($required as $field) {
        if (!array_key_exists($field, $data) || $data[$field] === null || $data[$field] === '') {
            $missing[] = $field;
        }
    }
    if (!empty($missing)) {
        sendResponse(400, 'error', 'Faltan los campos requeridos: ' . implode(', ', $missing));
    }
}

function sendResponse(int $httpCode, string $status, ?string $message, array $extra = []): never
{
    http_response_code($httpCode);
    $body = ['status' => $status];
    if ($message !== null) $body['message'] = $message;
    echo json_encode(array_merge($body, $extra), JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
    exit;
}
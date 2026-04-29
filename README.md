Primero que nada, para poder entrar al sistema por primera vez se tienen que introducir las siguientes credenciales:
usuario: ADMIN
contraseña: admin123

ARQUITECTURA DEL PROYECTO

src/main/java/mx/unison/
│
├── MainApp.java                 - Punto de entrada y configuración inicial
├── controller/                  - Lógica de control y eventos de UI
│   ├── HomeController.java
│   ├── LoginController.java
│   ├── AlmacenesController.java
│   └── ProductosController.java
│
├── model/                       - Entidades del sistema (POJOs)
│   ├── Usuario.java
│   ├── Almacen.java
│   └── Producto.java
│
├── dao/                         - Capa de acceso a datos (Interfaces e Impl)
│   ├── UsuarioDAO.java
│   ├── AlmacenDAO.java
│   ├── ProductoDAO.java
│   └── impl/                    - Implementaciones específicas con ORMLite
│
├── utils/                       - Clases de apoyo
│   ├── Navigation.java          - Gestión de cambio de escenas
│   └── DatabaseHelper.java      - Configuración de SQLite y tablas
│
└── resources/                   - Recursos visuales
    ├── views/                   - Archivos FXML
    └── styles/                  - Hojas de estilo CSS
    
---Punto de Entrada (MainApp)---
Clase principal encargada de inicializar la conexión a la base de datos y cargar la escena de autenticación.

---Controladores---
Actúan como intermediarios entre las vistas y los modelos, gestionando la lógica de negocio y las acciones del usuario.

---Vistas (FXML/CSS)---
Definen la interfaz gráfica de usuario. Se utiliza un archivo CSS global para aplicar el tema oscuro en toda la aplicación.

---Modelos (ORM)---
Clases que representan las tablas de la base de datos. Utilizan anotaciones de ORMLite para el mapeo objeto-relacional.

---Capa DAO (Data Access Object)---
Proporciona una interfaz abstracta para realizar operaciones CRUD, desacoplando la lógica de negocio del acceso directo a la base de datos.

---Persistencia---
Gestionada a través de SQLite, con el soporte de DatabaseHelper para la creación automática de esquemas y usuarios por defecto.

---Navegación---
Un componente centralizado que facilita el intercambio de vistas de forma fluida mediante el método setRoot.


# Képböngésző webalkalmazás

Java 21 és Spring Boot 3.5.6 alapú, fájlalapú H2 adatbázist használó képgaléria
szerepkör- és képformátum-jogosultságokkal.

## Futtatás

**Előfeltétel** a **Java 21** és a **Maven** telepítése:

```powershell
mvn spring-boot:run
```

Ezután az alkalmazás a `http://localhost:8080/login` címen érhető el.
Induláskor a `DataInitializer` létrehozza a `data` és `images-dir` könyvtárakat,
feltölti a tesztképeket és létrehozza a tesztfelhasználókat.

## Tesztfiókok

| Felhasználó    | Jelszó     | Jogosultság                       |
| -------------- | ---------- | --------------------------------- |
| `admin`        | `admin123` | minden formátum és adminisztráció |
| `user_jpg`     | `user123`  | jpg, jpeg                         |
| `user_png_gif` | `user123`  | png, gif                          |

## Fontos végpontok

- `/login` - bejelentkezés
- `/gallery` - jogosultság szerint szűrt galéria
- `/admin/users` - adminisztráció
- `/h2-console` - H2 konzol (`jdbc:h2:file:./data/imagebrowserdb`, `sa`, üres jelszó)

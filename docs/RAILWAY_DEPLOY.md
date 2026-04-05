# Deploy Railway cho Shop Gia Thinh

## 1. Tinh trang repo

- Repo da co file `railway.json` de Railway build va start app tu dong.
- `server.port` da doc tu bien `PORT`, phu hop voi Railway.
- App dang dong goi thanh file WAR executable va duoc start bang `java -jar target/*.war`.

## 2. Tao service tren Railway

1. Dang nhap Railway.
2. Chon `New Project`.
3. Chon `Deploy from GitHub repo`.
4. Chon repo `thinhweb3/ShopDoChoiGiaThinh`.
5. Chon branch `main`.
6. Doi Railway build xong deployment dau tien.

Neu Railway hoi build/start command, giu nguyen theo file `railway.json` trong repo.

## 3. Tao domain public

1. Vao service vua tao.
2. Mo tab `Settings`.
3. Mo muc `Networking`.
4. Bam `Generate Domain`.
5. Luu lai domain dang `https://...railway.app`.

## 4. Khai bao bien moi truong

Vao tab `Variables` cua service va them toi thieu:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `STORE_CONTACT_NAME`
- `STORE_CONTACT_PHONE`
- `STORE_CONTACT_ZALO`

Nen them them:

- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `PAYMENT_GMAIL_HOST`
- `PAYMENT_GMAIL_PORT`
- `PAYMENT_GMAIL_USERNAME`
- `PAYMENT_GMAIL_PASSWORD`
- `PAYMENT_GMAIL_FOLDER`
- `PAYMENT_GMAIL_LOOKBACK_MINUTES`
- `PAYMENT_GMAIL_MAX_MESSAGES`
- `PAYMENT_GMAIL_EXPECTED_SENDER`
- `PAYMENT_QR_BANK_ID`
- `PAYMENT_QR_ACCOUNT`
- `PAYMENT_QR_ACQ_NAME`
- `PAYMENT_QR_TEMPLATE`

Neu dung dang nhap Google, them dung ten bien sau:

- `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID`
- `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET`

## 5. Cau hinh luu anh de khong bi mat sau moi lan redeploy

Neu ban co upload anh tu trang admin, can gan `Volume`.

1. Tao `Volume` trong Railway va gan vao service.
2. Dat `Mount Path` la `/app/data`.
3. Dat bien moi truong `APP_STORAGE_UPLOAD_ROOT=/app/data/uploads`.

Neu khong gan volume, anh upload local se bi mat sau khi redeploy.

## 6. Cau hinh Google Login

Neu co dung Google Login:

1. Mo Google Cloud Console.
2. Vao OAuth Client dang dung.
3. Them Authorized redirect URI:
   - `https://<ten-domain-railway-cua-ban>/login/oauth2/code/google`
4. Luu lai.

## 7. Kiem tra sau deploy

Sau khi deploy xanh, mo cac URL sau:

- `/`
- `/home/index`
- `/product/list`
- `/auth/login`
- `/admin/login`

Neu dang nhap admin va upload anh duoc, service da o muc co the dung that.

## 8. Loi hay gap

- Build loi dependency Spring milestone:
  - Repo da duoc bo sung `spring-milestones` trong `pom.xml`. Neu van loi, thu `Redeploy` lai.
- Login Google khong hien:
  - Kiem tra lai 2 bien `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_*`.
- Anh bi mat sau redeploy:
  - Kiem tra volume co mount vao `/app/data` va `APP_STORAGE_UPLOAD_ROOT` da la `/app/data/uploads`.

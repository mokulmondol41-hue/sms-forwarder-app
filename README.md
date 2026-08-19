# SMS পেমেন্ট ফরওয়ার্ডার (Android, pure Java, phone-only build)

একটাই বাটন — **START**। চাপলে অ্যাপটা bKash/Nagad-এর "money received"
SMS আসামাত্র সেটা পড়ে আপনার Worker-এর `/api/sms/ingest`-এ পাঠিয়ে দেয়।
কোনো external library নেই — pure Android SDK দিয়ে লেখা, তাই PC ছাড়া
ফোনেই বিল্ড করা যায়।

## ১. বিল্ড করার আগে — Config.java এডিট করুন

`app/src/main/java/com/paylink/smsforwarder/Config.java` খুলে দুইটা
লাইন বদলান:

```java
public static final String API_BASE_URL = "https://your-worker.workers.dev";
public static final String API_KEY = "5b63732a0ba6258be7d1300226f2deeecfe43cfb6869efbb";
```

- `API_BASE_URL` — আপনার deploy করা Worker-এর আসল URL (শেষে `/` ছাড়া)।
- `API_KEY` — Worker-এর `src/index.js`-এর `CONFIG.API_KEY`-এর সাথে
  **হুবহু এক** হতে হবে।

## ২. ফোনে বিল্ড করা — AIDE দিয়ে

1. Play Store থেকে **AIDE - IDE for Android & Java** ইনস্টল করুন (ফ্রি)।
2. এই zip-টা এক্সট্র্যাক্ট করে ফোনের স্টোরেজে রাখুন (যেমন
   `Internal Storage/SmsForwarder/`)।
3. AIDE ওপেন করে **Open Project** → `SmsForwarder` ফোল্ডারটা সিলেক্ট করুন
   (এতে `build.gradle` আছে, AIDE এটা Gradle প্রজেক্ট হিসেবে চিনে নেবে)।
4. উপরে ▶ (Run/Build) বাটনে চাপুন — প্রথমবার Gradle/Android SDK কম্পোনেন্ট
   ডাউনলোড হতে একটু সময় লাগবে, ভালো ইন্টারনেট থাকলে করে নিন।
5. Build সফল হলে APK ইনস্টল হয়ে যাবে সরাসরি আপনার ফোনে।

**বিকল্প:** ভবিষ্যতে কোনো কম্পিউটার/Android Studio হাতে পেলে এই একই
ফোল্ডার সরাসরি "Open" করে সেখান থেকেও বিল্ড করতে পারবেন — কোনো পরিবর্তন
লাগবে না।

## ৩. প্রথমবার চালানো

1. অ্যাপ ওপেন করুন, **START** চাপুন।
2. SMS পড়ার পারমিশন চাইবে — **Allow** দিন।
3. (Android 13+ হলে) Notification পারমিশনও চাইবে — **Allow** দিন।
4. একটা persistent notification দেখাবে "SMS ফরওয়ার্ডার চলছে" — এটা
   স্বাভাবিক, এটা থাকলেই বুঝবেন অ্যাপ ব্যাকগ্রাউন্ডে সচল আছে।

## ৪. জরুরি — Battery Optimization বন্ধ করুন

Xiaomi (MIUI), Samsung, Oppo, Vivo-এর মতো ফোনে নিজস্ব battery-saver
agressive ভাবে ব্যাকগ্রাউন্ড অ্যাপ বন্ধ করে দেয়, এমনকি foreground
service থাকলেও। **এটা না করলে SMS মিস হতে পারে:**

- **সেটিংস → Apps → SMS পেমেন্ট ফরওয়ার্ডার → Battery** → "Unrestricted" /
  "No restrictions" সিলেক্ট করুন।
- Xiaomi হলে: **Settings → Apps → Manage apps → SMS পেমেন্ট ফরওয়ার্ডার →
  Autostart** চালু করুন, আর Battery saver-এ "No restrictions" দিন।
- Samsung হলে: **Settings → Battery → Background usage limits** থেকে
  অ্যাপটাকে "Never sleeping apps"-এ যোগ করুন।

## কীভাবে কাজ করে

- `SmsReceiver` সরাসরি Android-এর SMS broadcast শোনে (manifest-এ
  রেজিস্টার করা, তাই অ্যাপ বন্ধ থাকলেও সিস্টেম দরকার হলে জাগিয়ে দেয়)।
- প্রতিটা আসা SMS-এ `SmsParser` চেক করে: বডিতে "bkash"/"nagad" ও
  "received" শব্দ আছে কিনা, TrxID ও Tk এমাউন্ট বের করা যায় কিনা।
  না মিললে চুপচাপ ignore করে — অন্য কোনো SMS নিয়ে কিছু করে না।
- মিললে `ApiClient` ব্যাকগ্রাউন্ড থ্রেডে `POST /api/sms/ingest` কল করে।
- মেইন স্ক্রিনে সবসময় শেষ ফরওয়ার্ড করা SMS-এর status (success/fail)
  দেখাবে, ডিবাগ করা সহজ হওয়ার জন্য।
- **STOP** চাপলে নতুন কোনো SMS আর ফরওয়ার্ড হবে না (Config-এর ফ্ল্যাগ
  বন্ধ হয়ে যায়), আর persistent notification-ও চলে যায়।

## সীমাবদ্ধতা

- অ্যাপ বন্ধ/uninstalled অবস্থায় আসা SMS ধরতে পারবে না — এটা শুধু
  *live* আসা SMS শোনে, ইনবক্সের পুরনো SMS পড়ে না।
- ইন্টারনেট না থাকলে সেই মুহূর্তে ফরওয়ার্ড ব্যর্থ হবে (retry নেই এই
  সহজ ভার্সনে) — তবে কাস্টমার নিজে TrxID টাইপ করলে সেটা তখনও কাজ করবে,
  শুধু auto-verify-টা মিস হবে সেই একটা পেমেন্টে।
- একটা ফোন = একটা bKash/Nagad নাম্বারের SMS। একাধিক নাম্বার হলে
  প্রতিটার জন্য আলাদা ফোনে অ্যাপ চালাতে হবে (সবগুলো একই Worker-এ
  ইনগেস্ট করতে পারবে)।

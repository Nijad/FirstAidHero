CREATE TABLE IF NOT EXISTS FirstAidCases (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    symptoms TEXT NOT NULL,
    severity TEXT NOT NULL CHECK(severity IN ('critical','medium','minor')),
    is_common INTEGER NOT NULL DEFAULT 0,
    keywords TEXT DEFAULT ''
);

CREATE TABLE IF NOT EXISTS ActionSteps (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    case_id INTEGER NOT NULL,
    step_number INTEGER NOT NULL,
    instruction TEXT NOT NULL,
    FOREIGN KEY (case_id) REFERENCES FirstAidCases(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS EmergencyContacts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    phone TEXT NOT NULL,
    type TEXT NOT NULL DEFAULT 'custom',
    category TEXT
);

CREATE TABLE IF NOT EXISTS TreatedCases (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    case_id INTEGER,
    description TEXT NOT NULL,
    date TEXT NOT NULL,
    photo_path TEXT,
    procedures TEXT NOT NULL,
    FOREIGN KEY (case_id) REFERENCES FirstAidCases(id) ON DELETE SET NULL
);

CREATE INDEX idx_cases_title ON FirstAidCases(title);
CREATE INDEX idx_cases_severity ON FirstAidCases(severity);
CREATE INDEX idx_steps_case ON ActionSteps(case_id);
CREATE INDEX idx_treated_date ON TreatedCases(date);

INSERT INTO EmergencyContacts (name, phone, type, category) VALUES ('الإسعاف', '110', 'general', 'ambulance');
INSERT INTO EmergencyContacts (name, phone, type, category) VALUES ('الشرطة', '112', 'general', 'police');
INSERT INTO EmergencyContacts (name, phone, type, category) VALUES ('الإطفاء والدفاع المدني', '113', 'general', 'fire');
INSERT INTO EmergencyContacts (name, phone, type, category) VALUES ('الهلال الأحمر العربي السوري', '133', 'general', 'red_crescent');

INSERT INTO FirstAidCases (title, symptoms, severity, is_common, keywords) VALUES ('النزيف الحاد', 'خروج دم غزير من الجرح، شحوب البشرة، تسارع النبض، دوار، تعرق بارد', 'critical', 1, 'نزف نزيف دم جرح جراح قطع');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (1, 1, 'ارتدِ قفازات نظيفة إن أمكن لحماية نفسك من الأمراض المنقولة');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (1, 2, 'اضغط مباشرة على الجرح بقطعة قماش نظيفة أو شاش معقم');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (1, 3, 'ارفع الطرف المصاب فوق مستوى القلب إن لم يكن هناك كسر');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (1, 4, 'لا ترفع الضغط للتحقق من توقف النزيف بل أضف طبقة جديدة فوقه');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (1, 5, 'ثبّت الضمادة بشريط لاصق دون شد مفرط يعيق الدورة الدموية');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (1, 6, 'اتصل بالإسعاف على 110 وانقل المصاب للمستشفى فوراً');

INSERT INTO FirstAidCases (title, symptoms, severity, is_common, keywords) VALUES ('الإنعاش القلبي الرئوي', 'فقدان الوعي، توقف التنفس، عدم وجود نبض، شحوب أو ازرقاق', 'critical', 1, 'انعاش cpr قلب تنفس نبض توقف');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (2, 1, 'تأكد من أمان المكان قبل الاقتراب من المصاب');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (2, 2, 'نادِ على المصاب وحرّك كتفيه للتحقق من استجابته');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (2, 3, 'اطلب من شخص الاتصال بـ 110 وآخر بإحضار جهاز إزالة الرجفان إن وُجد');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (2, 4, 'ضع كعب يدك في منتصف الصدر واليد الأخرى فوقها');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (2, 5, 'اضغط بعمق 5-6 سم بمعدل 100-120 ضغطة في الدقيقة');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (2, 6, 'بعد 30 ضغطة أعطِ نفسين إنقاذيين ثم أعد الدورة حتى وصول الإسعاف');

INSERT INTO FirstAidCases (title, symptoms, severity, is_common, keywords) VALUES ('الاختناق', 'عدم القدرة على الكلام أو السعال، إمساك الحلق باليدين، ازرقاق الوجه', 'critical', 1, 'اختناق غصة شرقة غص بلع هيمليك');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (3, 1, 'اسأل المصاب هل يستطيع الكلام إن كان يستطيع شجعه على السعال');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (3, 2, 'إن لم يستطع الكلام قف خلفه وضع ذراعيك حول خصره');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (3, 3, 'أغلق يدك على شكل قبضة فوق السرة مباشرة');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (3, 4, 'اضغط بقوة نحو الداخل والأعلى 5 مرات (مناورة هيمليك)');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (3, 5, 'بدّل بين 5 ضربات على الظهر و5 ضغطات على البطن حتى خروج الجسم');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (3, 6, 'إن فقد المصاب وعيه ابدأ الإنعاش القلبي الرئوي فوراً واتصل بـ 110');

INSERT INTO FirstAidCases (title, symptoms, severity, is_common, keywords) VALUES ('الحروق', 'احمرار الجلد، ألم شديد، ظهور فقاعات، اسوداد في الحروق العميقة', 'medium', 1, 'حرق حروق حرقان لسعة نار حرارة سلق');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (4, 1, 'أبعد المصاب عن مصدر الحرارة وامنع انتشار الحريق');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (4, 2, 'برّد المنطقة المصابة بماء جارٍ فاتر لمدة 20 دقيقة');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (4, 3, 'لا تستخدم الثلج أو المعجون أو الزبدة على الحرق');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (4, 4, 'لا تفقأ الفقاعات التي تظهر على الجلد');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (4, 5, 'غطِّ الحرق بشاش معقم غير لاصق أو قماش نظيف مبلل');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (4, 6, 'راجع الطبيب فوراً إن كان الحرق كبيراً أو في الوجه أو المفاصل');

INSERT INTO FirstAidCases (title, symptoms, severity, is_common, keywords) VALUES ('الكسور', 'ألم شديد موضعي، تورم، تشوّه واضح، عدم القدرة على تحريك الطرف', 'medium', 1, 'كسر كسور فخد ذراع ساق عظم');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (5, 1, 'لا تحاول تحريك الطرف المصاب أو إعادة الكسر إلى مكانه');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (5, 2, 'ثبّت الطرف المصاب بالوضع الذي وجدته عليه');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (5, 3, 'استخدم مسطرة أو عصا أو صحيفة ملفوفة كجبيرة مؤقتة');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (5, 4, 'ضع كمادات باردة ملفوفة بقماش لتخفيف الألم والتورم');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (5, 5, 'ارفع الطرف المصاب إن أمكن إن لم يسبب ذلك ألماً إضافياً');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (5, 6, 'انقل المصاب للمستشفى لتلقي العلاج المناسب');

INSERT INTO FirstAidCases (title, symptoms, severity, is_common, keywords) VALUES ('الصدمة الكهربائية', 'فقدان الوعي، حروق عند نقاط التلامس، توقف القلب أو التنفس، تشنجات', 'critical', 0, 'كهرباء صعقة تيار صدمة كهربائية ماس');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (6, 1, 'لا تلمس المصاب قبل فصل التيار الكهربائي من المصدر الرئيسي');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (6, 2, 'استخدم عصا خشبية أو بلاستيكية لإبعاد السلك عن المصاب');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (6, 3, 'اتصل بالإسعاف على 110 فوراً واطلب المساعدة');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (6, 4, 'تحقق من التنفس والنبض وابدأ الإنعاش القلبي الرئوي إن لزم');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (6, 5, 'غطِّ الحروق بشاش معقم نظيف دون وضع أي مراهم');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (6, 6, 'ابقَ مع المصاب حتى وصول الطاقم الطبي');

INSERT INTO FirstAidCases (title, symptoms, severity, is_common, keywords) VALUES ('الغرق', 'صعوبة في التنفس، سعال مع رغوة، ازرقاق الشفاه، فقدان الوعي', 'critical', 0, 'غرق غريق ماء بحر بحيرة سباحة');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (7, 1, 'لا تدخل الماء بنفسك إن لم تكن مدرباً على الإنقاذ المائي');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (7, 2, 'ارمِ للمصاب حبل إسعاف أو عوامة أو أي جسم طافٍ');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (7, 3, 'بعد إخراجه من الماء تحقق من التنفس والنبض فوراً');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (7, 4, 'إن لم يتنفس ابدأ الإنعاش القلبي الرئوي مباشرة');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (7, 5, 'لا تحاول إخراج الماء من الرئتين بالضغط على البطن');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (7, 6, 'اتصل بـ 110 وانقل المصاب للمستشفى حتى لو بدا بحالة جيدة');

INSERT INTO FirstAidCases (title, symptoms, severity, is_common, keywords) VALUES ('التسمم', 'غثيان، قيء، تشنجات، صعوبة تنفس، اتساع أو تضييق حدقة العين', 'critical', 0, 'تسمم سم سام مادة سامة غاز ابتلاع');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (8, 1, 'لا تحاول إجبار المصاب على التقيؤ فقد يزيد الضرر');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (8, 2, 'حدد نوع المادة السامة والكمية إن أمكن');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (8, 3, 'احتفظ بعلبة المادة أو عينة من القيء لعرضها على الطبيب');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (8, 4, 'إن كان التسمم عبر الجلد اغسل المنطقة بماء جارٍ وصابون');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (8, 5, 'إن كان المصاب فاقداً للوعي ضعه على جانبه لمنع اختناقه');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (8, 6, 'اتصل بـ 110 فوراً أو بمركز مكافحة السموم');

INSERT INTO FirstAidCases (title, symptoms, severity, is_common, keywords) VALUES ('نوبة الصرع', 'فقدان الوعي، تشنجات عضلية، خروج رغوة من الفم، تبول لاإرادي', 'medium', 0, 'صرع تشنج كهرباء دماغ نوبة');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (9, 1, 'ابقَ هادئاً وأبعد الأجسام الصلبة عن المصاب');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (9, 2, 'ضع شيئاً ناعماً تحت رأسه لحمايته من الإصابة');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (9, 3, 'لا تحاول تثبيت المصاب أو إدخال أي شيء في فمه');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (9, 4, 'فكّ الملابس الضيقة حول الرقبة لتسهيل التنفس');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (9, 5, 'بعد انتهاء النوبة ضع المصاب على جانبه في وضع الإفاقة');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (9, 6, 'اتصل بـ 110 إن استمرت النوبة أكثر من 5 دقائق أو تكررت');

INSERT INTO FirstAidCases (title, symptoms, severity, is_common, keywords) VALUES ('ضربة الشمس', 'حرارة جسم مرتفعة، جلد جاف وساخن، غياب التعرق، صداع، فقدان وعي', 'medium', 1, 'شمس ضربة شمس حرارة اجهاد حراري جفاف');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (10, 1, 'انقل المصاب إلى مكان بارد وظليل بعيداً عن الشمس');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (10, 2, 'فكّ الملابس الزائدة واخلع الأحذية');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (10, 3, 'بلّل جسمه بماء فاتر واستخدم مروحة لتبريده تدريجياً');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (10, 4, 'ضع كمادات باردة على الرقبة والإبطين وأصل الفخذين');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (10, 5, 'إن كان واعياً أعطه ماءً بارداً ليشربه ببطء');
INSERT INTO ActionSteps (case_id, step_number, instruction) VALUES (10, 6, 'اتصل بـ 110 إن لم تتحسن حالته خلال 30 دقيقة');
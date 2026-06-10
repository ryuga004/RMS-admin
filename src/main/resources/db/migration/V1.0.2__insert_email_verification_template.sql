INSERT INTO email_templates (subject, template, placeholder_values)
VALUES (
    'Email Verification',
    'Hello, your verification code is {{verification_code}}. Use it to verify your email: {{email}}.',
    '["email", "verification_code"]'::jsonb
)
ON CONFLICT (subject) DO NOTHING;

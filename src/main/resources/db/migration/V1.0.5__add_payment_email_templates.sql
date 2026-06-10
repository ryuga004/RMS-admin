-- Payment Received Email (sent to owner)
INSERT INTO email_templates (subject, template, placeholder_values)
VALUES (
    'Payment Received',
    '<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Payment Received</title>
  <style>
    body { font-family: Arial, sans-serif; background: #f4f4f7; margin: 0; padding: 0; }
    .wrapper { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
    .header { background: #10b981; padding: 32px 40px; text-align: center; }
    .header h1 { color: #ffffff; margin: 0; font-size: 24px; }
    .body { padding: 32px 40px; color: #374151; }
    .body p { line-height: 1.6; margin: 0 0 16px; }
    .amount-box { background: #ecfdf5; border: 1px solid #6ee7b7; border-radius: 6px; padding: 16px 24px; margin: 24px 0; text-align: center; }
    .amount-box .amount { font-size: 32px; font-weight: bold; color: #059669; }
    .amount-box .label { font-size: 13px; color: #6b7280; margin-top: 4px; }
    .details { background: #f9fafb; border-radius: 6px; padding: 16px 24px; margin: 16px 0; }
    .details table { width: 100%; border-collapse: collapse; }
    .details td { padding: 6px 0; font-size: 14px; color: #374151; }
    .details td:first-child { color: #6b7280; width: 40%; }
    .footer { padding: 20px 40px; background: #f9fafb; text-align: center; font-size: 12px; color: #9ca3af; }
  </style>
</head>
<body>
  <div class="wrapper">
    <div class="header">
      <h1>💰 Payment Received</h1>
    </div>
    <div class="body">
      <p>Hi <strong>{{owner_name}}</strong>,</p>
      <p>Great news! A payment has been successfully processed for your property.</p>
      <div class="amount-box">
        <div class="amount">{{currency}} {{amount}}</div>
        <div class="label">Amount Received</div>
      </div>
      <div class="details">
        <table>
          <tr><td>Property</td><td><strong>{{asset_title}}</strong></td></tr>
          <tr><td>Payment Option</td><td>{{payment_option_name}}</td></tr>
          <tr><td>Paid By</td><td>{{tenant_name}} ({{tenant_email}})</td></tr>
          <tr><td>Payment Date</td><td>{{paid_at}}</td></tr>
          <tr><td>Transaction ID</td><td>{{payment_intent_id}}</td></tr>
        </table>
      </div>
      <p>The funds will be processed according to your payment provider schedule.</p>
    </div>
    <div class="footer">
      <p>This is an automated notification from RMS. Please do not reply to this email.</p>
    </div>
  </div>
</body>
</html>',
    '["owner_name", "currency", "amount", "asset_title", "payment_option_name", "tenant_name", "tenant_email", "paid_at", "payment_intent_id"]'::jsonb
);

-- Payment Receipt Email (sent to tenant)
INSERT INTO email_templates (subject, template, placeholder_values)
VALUES (
    'Payment Receipt',
    '<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Payment Receipt</title>
  <style>
    body { font-family: Arial, sans-serif; background: #f4f4f7; margin: 0; padding: 0; }
    .wrapper { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
    .header { background: #6366f1; padding: 32px 40px; text-align: center; }
    .header h1 { color: #ffffff; margin: 0; font-size: 24px; }
    .body { padding: 32px 40px; color: #374151; }
    .body p { line-height: 1.6; margin: 0 0 16px; }
    .amount-box { background: #eef2ff; border: 1px solid #a5b4fc; border-radius: 6px; padding: 16px 24px; margin: 24px 0; text-align: center; }
    .amount-box .amount { font-size: 32px; font-weight: bold; color: #4f46e5; }
    .amount-box .label { font-size: 13px; color: #6b7280; margin-top: 4px; }
    .details { background: #f9fafb; border-radius: 6px; padding: 16px 24px; margin: 16px 0; }
    .details table { width: 100%; border-collapse: collapse; }
    .details td { padding: 6px 0; font-size: 14px; color: #374151; }
    .details td:first-child { color: #6b7280; width: 40%; }
    .badge { display: inline-block; background: #dcfce7; color: #16a34a; padding: 4px 12px; border-radius: 9999px; font-size: 13px; font-weight: 600; }
    .footer { padding: 20px 40px; background: #f9fafb; text-align: center; font-size: 12px; color: #9ca3af; }
  </style>
</head>
<body>
  <div class="wrapper">
    <div class="header">
      <h1>🧾 Payment Receipt</h1>
    </div>
    <div class="body">
      <p>Hi <strong>{{tenant_name}}</strong>,</p>
      <p>Your payment has been successfully processed. Please keep this receipt for your records.</p>
      <div class="amount-box">
        <div class="amount">{{currency}} {{amount}}</div>
        <div class="label">Amount Paid &nbsp; <span class="badge">✓ Confirmed</span></div>
      </div>
      <div class="details">
        <table>
          <tr><td>Property</td><td><strong>{{asset_title}}</strong></td></tr>
          <tr><td>Payment Option</td><td>{{payment_option_name}}</td></tr>
          <tr><td>Payment Date</td><td>{{paid_at}}</td></tr>
          <tr><td>Transaction ID</td><td>{{payment_intent_id}}</td></tr>
          <tr><td>Status</td><td><strong style="color:#16a34a">Completed</strong></td></tr>
        </table>
      </div>
      <p>If you have any questions about this payment, please contact your property owner.</p>
    </div>
    <div class="footer">
      <p>This is an automated receipt from RMS. Please do not reply to this email.</p>
    </div>
  </div>
</body>
</html>',
    '["tenant_name", "currency", "amount", "asset_title", "payment_option_name", "paid_at", "payment_intent_id"]'::jsonb
);

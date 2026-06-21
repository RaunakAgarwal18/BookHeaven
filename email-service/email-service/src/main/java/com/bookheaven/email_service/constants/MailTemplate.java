package com.bookheaven.email_service.constants;

public class MailTemplate {
    public static final String OTP_MAIL = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
    </head>
    <body style="margin:0; padding:0; font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color:#f4f6f8;">
      <table width="100%" cellspacing="0" cellpadding="0" style="padding:40px 20px;">
        <tr>
          <td align="center">
            <table width="540" cellspacing="0" cellpadding="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 10px 25px -5px rgba(0,0,0,0.1);">
              <!-- Header -->
              <tr>
                <td align="center" style="background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding:35px 20px;">
                  <div style="font-size:32px; margin-bottom:10px;">🔐</div>
                  <div style="font-size:24px; font-weight:800; color:#ffffff; letter-spacing: -0.5px;">BookHeaven</div>
                  <div style="font-size:15px; color:#e0e7ff; margin-top:5px; font-weight:500;">Account Verification</div>
                </td>
              </tr>
              <!-- Body -->
              <tr>
                <td style="padding:40px 35px; color:#334155; line-height:1.6; font-size:15px;">
                  <p style="margin-top:0;">Hello,</p>
                  <p>Here is your One-Time Password (OTP) to securely access your account:</p>
                  <div style="text-align:center; margin:30px 0;">
                    <div style="display:inline-block; padding:15px 30px; font-size:28px; letter-spacing:6px; font-weight:800; background:#f0fdf4; color:#16a34a; border:2px dashed #bbf7d0; border-radius:8px;">
                      {{OTP}}
                    </div>
                  </div>
                  <p style="color:#64748b; font-size:14px; text-align:center;">⏳ This code will expire in <b>5 minutes</b>.</p>
                  <p style="color:#94a3b8; font-size:13px; margin-top:30px;">If you didn't request this code, you can safely ignore this email.</p>
                </td>
              </tr>
              <!-- Footer -->
              <tr>
                <td style="background:#f8fafc; padding:24px 35px; border-top:1px solid #e2e8f0; text-align:center;">
                  <div style="font-size:13px; color:#64748b; font-weight:500;">&copy; 2026 BookHeaven. All rights reserved.</div>
                  <div style="font-size:12px; color:#94a3b8; margin-top:8px;">Have questions? Reply to this email or visit our support center.</div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
""";

    public static final String PASSWORD_RESET_MAIL = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
    </head>
    <body style="margin:0; padding:0; font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color:#f4f6f8;">
      <table width="100%" cellspacing="0" cellpadding="0" style="padding:40px 20px;">
        <tr>
          <td align="center">
            <table width="540" cellspacing="0" cellpadding="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 10px 25px -5px rgba(0,0,0,0.1);">
              <!-- Header -->
              <tr>
                <td align="center" style="background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding:35px 20px;">
                  <div style="font-size:32px; margin-bottom:10px;">🔑</div>
                  <div style="font-size:24px; font-weight:800; color:#ffffff; letter-spacing: -0.5px;">BookHeaven</div>
                  <div style="font-size:15px; color:#e0e7ff; margin-top:5px; font-weight:500;">Password Reset Request</div>
                </td>
              </tr>
              <!-- Body -->
              <tr>
                <td style="padding:40px 35px; color:#334155; line-height:1.6; font-size:15px;">
                  <p style="margin-top:0;">Hello,</p>
                  <p>We received a request to reset the password for your BookHeaven account. Click the button below to securely set a new password:</p>
                  <div style="text-align:center; margin:35px 0;">
                    <a href="{{RESET_LINK}}" style="display:inline-block; padding:14px 32px; background:#4f46e5; color:#ffffff; font-size:15px; font-weight:600; text-decoration:none; border-radius:8px; box-shadow:0 4px 6px -1px rgba(79, 70, 229, 0.4);">Reset My Password</a>
                  </div>
                  <p style="color:#64748b; font-size:14px; text-align:center;">⏳ This link is valid for <b>30 minutes</b>.</p>
                  <div style="margin-top:35px; border-top:1px solid #e2e8f0; padding-top:20px;">
                    <p style="color:#94a3b8; font-size:13px; margin:0;">If the button doesn't work, copy and paste this link into your browser:</p>
                    <a href="{{RESET_LINK}}" style="font-size:12px; color:#4f46e5; word-break:break-all; text-decoration:none;">{{RESET_LINK}}</a>
                  </div>
                </td>
              </tr>
              <!-- Footer -->
              <tr>
                <td style="background:#f8fafc; padding:24px 35px; border-top:1px solid #e2e8f0; text-align:center;">
                  <div style="font-size:13px; color:#64748b; font-weight:500;">&copy; 2026 BookHeaven. All rights reserved.</div>
                  <div style="font-size:12px; color:#94a3b8; margin-top:8px;">Have questions? Reply to this email or visit our support center.</div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
""";

    public static final String ORDER_CONFIRMED_MAIL = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
    </head>
    <body style="margin:0; padding:0; font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color:#f4f6f8;">
      <table width="100%" cellspacing="0" cellpadding="0" style="padding:40px 20px;">
        <tr>
          <td align="center">
            <table width="540" cellspacing="0" cellpadding="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 10px 25px -5px rgba(0,0,0,0.1);">
              <!-- Header -->
              <tr>
                <td align="center" style="background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding:35px 20px;">
                  <div style="font-size:32px; margin-bottom:10px;">✨</div>
                  <div style="font-size:24px; font-weight:800; color:#ffffff; letter-spacing: -0.5px;">BookHeaven</div>
                  <div style="font-size:15px; color:#e0e7ff; margin-top:5px; font-weight:500;">Order Confirmed</div>
                </td>
              </tr>
              <!-- Body -->
              <tr>
                <td style="padding:40px 35px; color:#334155; line-height:1.6; font-size:15px;">
                  <p style="margin-top:0; font-size:16px;">Hi <b>{{USERNAME}}</b>,</p>
                  <p>Thank you for your purchase! We've successfully received your order and are currently processing it.</p>
                  
                  <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:20px; margin:25px 0; text-align:center;">
                    <div style="font-size:13px; color:#64748b; text-transform:uppercase; letter-spacing:1px; font-weight:600;">Order Number</div>
                    <div style="font-size:22px; font-weight:700; color:#4f46e5; margin-top:8px;">#{{ORDER_ID}}</div>
                  </div>

                  <div style="border-left:4px solid #4f46e5; padding-left:15px; margin:25px 0;">
                    <div style="font-weight:600; color:#1e293b; margin-bottom:5px;">📍 Shipping Address</div>
                    <div style="color:#475569; font-size:14px; line-height:1.5;">{{SHIPPING_ADDRESS}}</div>
                  </div>
                  
                  <p style="color:#64748b; font-size:14px;">We'll send you another email as soon as your order ships.</p>
                </td>
              </tr>
              <!-- Footer -->
              <tr>
                <td style="background:#f8fafc; padding:24px 35px; border-top:1px solid #e2e8f0; text-align:center;">
                  <div style="font-size:13px; color:#64748b; font-weight:500;">&copy; 2026 BookHeaven. All rights reserved.</div>
                  <div style="font-size:12px; color:#94a3b8; margin-top:8px;">Have questions? Reply to this email or visit our support center.</div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
""";

    public static final String PAYMENT_FAILED_MAIL = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
    </head>
    <body style="margin:0; padding:0; font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color:#f4f6f8;">
      <table width="100%" cellspacing="0" cellpadding="0" style="padding:40px 20px;">
        <tr>
          <td align="center">
            <table width="540" cellspacing="0" cellpadding="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 10px 25px -5px rgba(0,0,0,0.1);">
              <!-- Header -->
              <tr>
                <td align="center" style="background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding:35px 20px;">
                  <div style="font-size:32px; margin-bottom:10px;">💳</div>
                  <div style="font-size:24px; font-weight:800; color:#ffffff; letter-spacing: -0.5px;">BookHeaven</div>
                  <div style="font-size:15px; color:#e0e7ff; margin-top:5px; font-weight:500;">Payment Failed</div>
                </td>
              </tr>
              <!-- Body -->
              <tr>
                <td style="padding:40px 35px; color:#334155; line-height:1.6; font-size:15px;">
                  <p style="margin-top:0; font-size:16px;">Hi <b>{{USERNAME}}</b>,</p>
                  <p>Unfortunately, we couldn't process the payment for your recent order.</p>
                  
                  <div style="background:#fef2f2; border:1px solid #fecaca; border-radius:8px; padding:20px; margin:25px 0; text-align:center;">
                    <div style="font-size:13px; color:#991b1b; text-transform:uppercase; letter-spacing:1px; font-weight:600;">Order Status: Cancelled</div>
                    <div style="font-size:18px; font-weight:700; color:#ef4444; margin-top:8px;">Order #{{ORDER_ID}}</div>
                  </div>

                  <p style="color:#475569;">Since the payment failed or timed out, your order has been automatically cancelled. No charges were made to your account.</p>
                  <p style="color:#64748b; font-size:14px; margin-top:20px;">You can easily place the order again by returning to your cart.</p>
                </td>
              </tr>
              <!-- Footer -->
              <tr>
                <td style="background:#f8fafc; padding:24px 35px; border-top:1px solid #e2e8f0; text-align:center;">
                  <div style="font-size:13px; color:#64748b; font-weight:500;">&copy; 2026 BookHeaven. All rights reserved.</div>
                  <div style="font-size:12px; color:#94a3b8; margin-top:8px;">Have questions? Reply to this email or visit our support center.</div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
""";

    public static final String WELCOME_MAIL = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
    </head>
    <body style="margin:0; padding:0; font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color:#f4f6f8;">
      <table width="100%" cellspacing="0" cellpadding="0" style="padding:40px 20px;">
        <tr>
          <td align="center">
            <table width="540" cellspacing="0" cellpadding="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 10px 25px -5px rgba(0,0,0,0.1);">
              <!-- Header -->
              <tr>
                <td align="center" style="background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding:35px 20px;">
                  <div style="font-size:32px; margin-bottom:10px;">👋</div>
                  <div style="font-size:24px; font-weight:800; color:#ffffff; letter-spacing: -0.5px;">BookHeaven</div>
                  <div style="font-size:15px; color:#e0e7ff; margin-top:5px; font-weight:500;">Welcome Aboard</div>
                </td>
              </tr>
              <!-- Body -->
              <tr>
                <td style="padding:40px 35px; color:#334155; line-height:1.6; font-size:15px;">
                  <p style="margin-top:0; font-size:16px;">Hi <b>{{USERNAME}}</b>,</p>
                  <p>Welcome to <b>BookHeaven</b>! We are thrilled to have you join our community of passionate readers.</p>
                  
                  <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:25px; margin:30px 0;">
                    <h3 style="margin:0 0 15px 0; color:#1e293b; font-size:16px;">📚 What's next?</h3>
                    <ul style="margin:0; padding-left:20px; color:#475569; line-height:1.8; font-size:14px;">
                      <li><b>Discover</b> thousands of books across all genres</li>
                      <li><b>Create</b> your personal wishlist for future reads</li>
                      <li><b>Order</b> physical books delivered straight to your door</li>
                      <li><b>Connect</b> with our curated library collection</li>
                    </ul>
                  </div>

                  <p style="color:#64748b; font-size:14px;">Log in today and discover your next great read!</p>
                </td>
              </tr>
              <!-- Footer -->
              <tr>
                <td style="background:#f8fafc; padding:24px 35px; border-top:1px solid #e2e8f0; text-align:center;">
                  <div style="font-size:13px; color:#64748b; font-weight:500;">&copy; 2026 BookHeaven. All rights reserved.</div>
                  <div style="font-size:12px; color:#94a3b8; margin-top:8px;">Have questions? Reply to this email or visit our support center.</div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
""";

    public static final String SELLER_ORDER_NOTIFICATION_MAIL = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
    </head>
    <body style="margin:0; padding:0; font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color:#f4f6f8;">
      <table width="100%" cellspacing="0" cellpadding="0" style="padding:40px 20px;">
        <tr>
          <td align="center">
            <table width="540" cellspacing="0" cellpadding="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 10px 25px -5px rgba(0,0,0,0.1);">
              <!-- Header -->
              <tr>
                <td align="center" style="background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding:35px 20px;">
                  <div style="font-size:32px; margin-bottom:10px;">🎉</div>
                  <div style="font-size:24px; font-weight:800; color:#ffffff; letter-spacing: -0.5px;">BookHeaven</div>
                  <div style="font-size:15px; color:#e0e7ff; margin-top:5px; font-weight:500;">New Order Received</div>
                </td>
              </tr>
              <!-- Body -->
              <tr>
                <td style="padding:40px 35px; color:#334155; line-height:1.6; font-size:15px;">
                  <p style="margin-top:0; font-size:16px;">Hi <b>{{SELLER_USERNAME}}</b>,</p>
                  <p>Great news! You just received a new order for your listings.</p>
                  
                  <div style="background:#ffffff; border:1px solid #e2e8f0; border-radius:8px; margin:25px 0; overflow:hidden;">
                    <div style="background:#f8fafc; padding:15px 20px; border-bottom:1px solid #e2e8f0; font-weight:600; color:#1e293b;">
                      Order #{{ORDER_ID}}
                    </div>
                    <div style="padding:20px;">
                      {{ITEMS_ROWS}}
                    </div>
                    <div style="background:#f0fdf4; padding:15px 20px; border-top:1px solid #e2e8f0; display:flex; justify-content:space-between; align-items:center;">
                      <span style="font-weight:600; color:#166534;">Your Earnings:</span>
                      <span style="font-weight:700; color:#15803d; font-size:18px;">{{CURRENCY}} {{TOTAL_AMOUNT}}</span>
                    </div>
                  </div>

                  <div style="border-left:4px solid #4f46e5; padding-left:15px; margin:25px 0; background:#f8fafc; padding:15px;">
                    <div style="font-weight:600; color:#1e293b; margin-bottom:5px;">👤 Buyer Info</div>
                    <div style="color:#475569; font-size:14px; margin-bottom:8px;"><b>Username:</b> @{{BUYER_USERNAME}}</div>
                    <div style="font-weight:600; color:#1e293b; margin-bottom:5px; margin-top:10px;">📍 Shipping To</div>
                    <div style="color:#475569; font-size:14px; line-height:1.5;">{{SHIPPING_ADDRESS}}</div>
                  </div>

                  <p style="color:#64748b; font-size:14px; margin-top:25px;">Please securely pack the items and mark the order as <b>Shipped</b> in your Seller Dashboard once dispatched.</p>
                </td>
              </tr>
              <!-- Footer -->
              <tr>
                <td style="background:#f8fafc; padding:24px 35px; border-top:1px solid #e2e8f0; text-align:center;">
                  <div style="font-size:13px; color:#64748b; font-weight:500;">&copy; 2026 BookHeaven. All rights reserved.</div>
                  <div style="font-size:12px; color:#94a3b8; margin-top:8px;">Have questions? Reply to this email or visit our support center.</div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
""";

    public static final String WELCOME_SELLER_MAIL = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
    </head>
    <body style="margin:0; padding:0; font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color:#f4f6f8;">
      <table width="100%" cellspacing="0" cellpadding="0" style="padding:40px 20px;">
        <tr>
          <td align="center">
            <table width="540" cellspacing="0" cellpadding="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 10px 25px -5px rgba(0,0,0,0.1);">
              <!-- Header -->
              <tr>
                <td align="center" style="background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding:35px 20px;">
                  <div style="font-size:32px; margin-bottom:10px;">🏪</div>
                  <div style="font-size:24px; font-weight:800; color:#ffffff; letter-spacing: -0.5px;">BookHeaven</div>
                  <div style="font-size:15px; color:#e0e7ff; margin-top:5px; font-weight:500;">Merchant Hub Access</div>
                </td>
              </tr>
              <!-- Body -->
              <tr>
                <td style="padding:40px 35px; color:#334155; line-height:1.6; font-size:15px;">
                  <p style="margin-top:0; font-size:16px;">Hi <b>{{USERNAME}}</b>,</p>
                  <p>Welcome to the <b>BookHeaven Merchant Hub</b>! Your seller account is now active.</p>
                  
                  <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; padding:25px; margin:30px 0;">
                    <h3 style="margin:0 0 15px 0; color:#1e293b; font-size:16px;">🚀 Quick Start Guide</h3>
                    <ul style="margin:0; padding-left:20px; color:#475569; line-height:1.8; font-size:14px;">
                      <li><b>List Inventory:</b> Add your books with custom pricing</li>
                      <li><b>Manage Orders:</b> View and fulfill incoming purchases</li>
                      <li><b>Connect Payouts:</b> Link your Razorpay account to receive funds</li>
                      <li><b>Track Earnings:</b> Monitor your daily sales analytics</li>
                    </ul>
                  </div>

                  <p style="color:#64748b; font-size:14px;">Head over to your Seller Dashboard to create your first listing. Wishing you high volumes of sales!</p>
                </td>
              </tr>
              <!-- Footer -->
              <tr>
                <td style="background:#f8fafc; padding:24px 35px; border-top:1px solid #e2e8f0; text-align:center;">
                  <div style="font-size:13px; color:#64748b; font-weight:500;">&copy; 2026 BookHeaven. All rights reserved.</div>
                  <div style="font-size:12px; color:#94a3b8; margin-top:8px;">Have questions? Reply to this email or visit our support center.</div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
""";

    public static final String ORDER_SHIPPED_MAIL = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
    </head>
    <body style="margin:0; padding:0; font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color:#f4f6f8;">
      <table width="100%" cellspacing="0" cellpadding="0" style="padding:40px 20px;">
        <tr>
          <td align="center">
            <table width="540" cellspacing="0" cellpadding="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 10px 25px -5px rgba(0,0,0,0.1);">
              <!-- Header -->
              <tr>
                <td align="center" style="background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding:35px 20px;">
                  <div style="font-size:32px; margin-bottom:10px;">🚚</div>
                  <div style="font-size:24px; font-weight:800; color:#ffffff; letter-spacing: -0.5px;">BookHeaven</div>
                  <div style="font-size:15px; color:#e0e7ff; margin-top:5px; font-weight:500;">Order Shipped</div>
                </td>
              </tr>
              <!-- Body -->
              <tr>
                <td style="padding:40px 35px; color:#334155; line-height:1.6; font-size:15px;">
                  <p style="margin-top:0; font-size:16px;">Hi <b>{{USERNAME}}</b>,</p>
                  <p>Good news! Your order <b>#{{ORDER_ID}}</b> has been packaged and handed over to our delivery partners.</p>
                  
                  <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; margin:25px 0; overflow:hidden;">
                    <div style="background:#e0e7ff; padding:15px 20px; border-bottom:1px solid #c7d2fe; font-weight:600; color:#3730a3;">
                      📦 Items in this shipment
                    </div>
                    <div style="padding:20px;">
                      {{ITEMS_ROWS}}
                    </div>
                  </div>

                  <div style="border-left:4px solid #4f46e5; padding-left:15px; margin:25px 0;">
                    <div style="font-weight:600; color:#1e293b; margin-bottom:5px;">📍 Delivering To</div>
                    <div style="color:#475569; font-size:14px; line-height:1.5;">{{SHIPPING_ADDRESS}}</div>
                  </div>
                  
                  <p style="color:#64748b; font-size:14px;">You will receive a final update once your package has been delivered to your doorstep.</p>
                </td>
              </tr>
              <!-- Footer -->
              <tr>
                <td style="background:#f8fafc; padding:24px 35px; border-top:1px solid #e2e8f0; text-align:center;">
                  <div style="font-size:13px; color:#64748b; font-weight:500;">&copy; 2026 BookHeaven. All rights reserved.</div>
                  <div style="font-size:12px; color:#94a3b8; margin-top:8px;">Have questions? Reply to this email or visit our support center.</div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
""";

    public static final String ORDER_DELIVERED_MAIL = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
    </head>
    <body style="margin:0; padding:0; font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color:#f4f6f8;">
      <table width="100%" cellspacing="0" cellpadding="0" style="padding:40px 20px;">
        <tr>
          <td align="center">
            <table width="540" cellspacing="0" cellpadding="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 10px 25px -5px rgba(0,0,0,0.1);">
              <!-- Header -->
              <tr>
                <td align="center" style="background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding:35px 20px;">
                  <div style="font-size:32px; margin-bottom:10px;">📦</div>
                  <div style="font-size:24px; font-weight:800; color:#ffffff; letter-spacing: -0.5px;">BookHeaven</div>
                  <div style="font-size:15px; color:#e0e7ff; margin-top:5px; font-weight:500;">Order Delivered</div>
                </td>
              </tr>
              <!-- Body -->
              <tr>
                <td style="padding:40px 35px; color:#334155; line-height:1.6; font-size:15px;">
                  <p style="margin-top:0; font-size:16px;">Hi <b>{{USERNAME}}</b>,</p>
                  <p>Your package has arrived! Order <b>#{{ORDER_ID}}</b> was successfully delivered to your address.</p>
                  
                  <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:8px; margin:25px 0; overflow:hidden;">
                    <div style="background:#dcfce7; padding:15px 20px; border-bottom:1px solid #bbf7d0; font-weight:600; color:#166534;">
                      ✅ Delivered Items
                    </div>
                    <div style="padding:20px;">
                      {{ITEMS_ROWS}}
                    </div>
                  </div>

                  <div style="border-left:4px solid #16a34a; padding-left:15px; margin:25px 0;">
                    <div style="font-weight:600; color:#1e293b; margin-bottom:5px;">📍 Delivered At</div>
                    <div style="color:#475569; font-size:14px; line-height:1.5;">{{SHIPPING_ADDRESS}}</div>
                  </div>
                  
                  <p style="color:#64748b; font-size:14px;">We hope you enjoy your new books! Don't forget to leave a review for the sellers on BookHeaven.</p>
                </td>
              </tr>
              <!-- Footer -->
              <tr>
                <td style="background:#f8fafc; padding:24px 35px; border-top:1px solid #e2e8f0; text-align:center;">
                  <div style="font-size:13px; color:#64748b; font-weight:500;">&copy; 2026 BookHeaven. All rights reserved.</div>
                  <div style="font-size:12px; color:#94a3b8; margin-top:8px;">Have questions? Reply to this email or visit our support center.</div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
""";

    public static final String MISSING_RAZORPAY_MAIL = """
    <!DOCTYPE html>
    <html>
    <head>
      <meta charset="UTF-8">
    </head>
    <body style="margin:0; padding:0; font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color:#f4f6f8;">
      <table width="100%" cellspacing="0" cellpadding="0" style="padding:40px 20px;">
        <tr>
          <td align="center">
            <table width="540" cellspacing="0" cellpadding="0" style="background:#ffffff; border-radius:12px; overflow:hidden; box-shadow:0 10px 25px -5px rgba(0,0,0,0.1);">
              <!-- Header -->
              <tr>
                <td align="center" style="background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding:35px 20px;">
                  <div style="font-size:32px; margin-bottom:10px;">⚠️</div>
                  <div style="font-size:24px; font-weight:800; color:#ffffff; letter-spacing: -0.5px;">BookHeaven</div>
                  <div style="font-size:15px; color:#e0e7ff; margin-top:5px; font-weight:500;">Action Required: Payout</div>
                </td>
              </tr>
              <!-- Body -->
              <tr>
                <td style="padding:40px 35px; color:#334155; line-height:1.6; font-size:15px;">
                  <p style="margin-top:0; font-size:16px;">Hi <b>{{USERNAME}}</b>,</p>
                  <p>We attempted to process your automated payout, but your account is missing a <b>Razorpay Account ID</b>.</p>
                  
                  <div style="background:#fffbeb; border:1px solid #fde68a; border-radius:8px; padding:20px; margin:25px 0; text-align:center;">
                    <div style="font-size:13px; color:#b45309; text-transform:uppercase; letter-spacing:1px; font-weight:600;">Pending Payout</div>
                    <div style="font-size:24px; font-weight:700; color:#d97706; margin-top:8px;">{{CURRENCY}} {{AMOUNT}}</div>
                  </div>

                  <p style="color:#475569;">To receive your pending funds, please log in to your Seller Dashboard and update your profile with a valid Razorpay Account ID. Your funds are safe and will be disbursed in the next cycle once updated.</p>
                </td>
              </tr>
              <!-- Footer -->
              <tr>
                <td style="background:#f8fafc; padding:24px 35px; border-top:1px solid #e2e8f0; text-align:center;">
                  <div style="font-size:13px; color:#64748b; font-weight:500;">&copy; 2026 BookHeaven. All rights reserved.</div>
                  <div style="font-size:12px; color:#94a3b8; margin-top:8px;">Have questions? Reply to this email or visit our support center.</div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
    </body>
    </html>
""";

}

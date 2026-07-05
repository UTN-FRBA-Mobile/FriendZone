import { Controller, Get, Header, Param } from '@nestjs/common';
import { ApiExcludeController } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';

/**
 * Serves the Android App Links verification file and a web fallback page for
 * invite links. Both routes are public (no JWT) because they are hit by the
 * Android system verifier and by browsers when the app is not installed.
 */
@ApiExcludeController()
@Controller()
export class InviteController {
  /**
   * Digital Asset Links file required by Android to verify https App Links.
   * Fingerprints come from the ANDROID_SHA256_CERT_FINGERPRINTS env var
   * (comma or whitespace separated SHA-256 signing cert fingerprints).
   */
  @Public()
  @Get('.well-known/assetlinks.json')
  @Header('Content-Type', 'application/json')
  assetLinks(): unknown {
    const fingerprints = (process.env.ANDROID_SHA256_CERT_FINGERPRINTS ?? '')
      .split(/[\s,]+/)
      .map((fp) => fp.trim())
      .filter((fp) => fp.length > 0);

    return [
      {
        relation: ['delegate_permission/common.handle_all_urls'],
        target: {
          namespace: 'android_app',
          package_name:
            process.env.ANDROID_PACKAGE_NAME ?? 'com.example.friendzone',
          sha256_cert_fingerprints: fingerprints,
        },
      },
    ];
  }

  /**
   * Web fallback shown when the invite link is opened without the app
   * installed (e.g. on desktop). When the app is installed and the link is
   * verified, Android opens the app directly and this route is never hit.
   */
  @Public()
  @Get('invite/:username')
  @Header('Content-Type', 'text/html; charset=utf-8')
  invitePage(@Param('username') username: string): string {
    const safe = escapeHtml(username);
    return `<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>FriendZone · Invitation</title>
    <style>
      body { font-family: -apple-system, Segoe UI, Roboto, sans-serif; background: #f5f5f4; color: #1c1917; margin: 0; display: flex; min-height: 100vh; align-items: center; justify-content: center; }
      .card { background: #fff; border: 1.5px solid #e7e5e4; border-radius: 16px; padding: 32px; max-width: 360px; text-align: center; }
      h1 { font-size: 22px; margin: 0 0 8px; }
      p { color: #78716c; font-size: 15px; line-height: 1.5; }
      .user { font-weight: 600; color: #1c1917; }
    </style>
  </head>
  <body>
    <div class="card">
      <h1>FriendZone</h1>
      <p><span class="user">@${safe}</span> invited you to be their friend.</p>
      <p>Install FriendZone and open this link from your phone to add them automatically.</p>
    </div>
  </body>
</html>`;
  }
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

package zone.moddev.yuki.updatenotifiers.blockbench;

record GithubRelease(String html_url, String name, boolean prerelease, String tag_name, String body,
                     String published_at) {
}

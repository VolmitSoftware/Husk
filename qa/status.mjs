export default {
  name: 'husk-status',
  description: 'Verify root and status commands return the expected runtime status.',
  async run(context) {
    context.expect(typeof context.options.expect === 'string', '--expect is required')
    const command = context.options.command ?? '/husk'
    const expected = new RegExp(context.options.expect)
    for (const invocation of [command, `${command} status`]) {
      await context.step(`verify ${invocation}`, async () => {
        const response = await context.command(invocation, expected, 10000)
        context.expect(typeof response === 'string', 'Runtime status was not received')
      })
    }
    await context.step('verify stable connection after commands', async () => {
      await context.sleep(15000)
      context.expect(context.bot.player !== undefined, 'Player disconnected after the status commands')
      context.expect(context.bot.health > 0, 'Player is not alive after the status commands')
    })
  }
}

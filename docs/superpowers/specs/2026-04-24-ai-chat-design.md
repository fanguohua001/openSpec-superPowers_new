# AI Chat Design

## Goal

Add a persistent AI chat feature to the RuoYi management system. The first version appears under the existing System Tools menu as AI Chat, supports streaming model responses, stores user sessions and messages, and reads model provider settings from `sys_config`.

The provider protocol is not guaranteed, so the implementation uses a small adapter boundary. The default adapter targets OpenAI Chat Completions compatible streaming APIs.

## Scope

In scope:

- AI chat menu entry under the existing System Tools menu.
- Per-user chat sessions and message history.
- Streaming assistant responses.
- Model configuration from `sys_config`.
- Default OpenAI-compatible streaming adapter.
- Basic permission checks for list, send, and delete operations.
- Failure persistence for model or stream errors.

Out of scope:

- Multi-provider management pages.
- Knowledge base, file upload, web search, and retrieval augmented generation.
- Token-accurate context trimming.
- Cross-request server-side cancellation registry.
- A dedicated model configuration table.

## Architecture

The feature has four layers.

1. Frontend chat page

   Add `ruoyi-ui/src/views/tool/aiChat/index.vue` and `ruoyi-ui/src/api/tool/aiChat.js`. The page contains a session list, message view, input area, send button, and stop button. During streaming, the assistant message is appended incrementally.

2. Backend controller

   Add an `AiChatController` under `ruoyi-admin`. It exposes session, message, delete, and streaming send endpoints under `/tool/aiChat`. Permissions follow the existing RuoYi `@PreAuthorize("@ss.hasPermi(...)")` style.

3. Business and persistence layer

   Add AI chat domain, mapper, and service classes under `ruoyi-system`. The service owns session validation, user isolation, message persistence, context loading, and assistant message status updates.

4. Model adapter layer

   Add a lightweight adapter boundary used by the service. The first adapter reads provider settings from `sys_config`, sends a streaming chat request to an OpenAI-compatible endpoint, parses SSE chunks, and calls back with response deltas. If the actual provider later differs, add a new adapter without changing the controller or frontend contract.

## Data Model

Add `ai_chat_session`:

- `session_id`: primary key.
- `user_id`: owner user ID. All reads and deletes filter by this field.
- `title`: session title. The default title is derived from the first user message.
- `model`: model name used for the session or latest send.
- `status`: `0` normal, `1` deleted or disabled.
- `create_by`, `create_time`, `update_by`, `update_time`, `remark`: RuoYi-style audit fields.

Add `ai_chat_message`:

- `message_id`: primary key.
- `session_id`: owning session.
- `user_id`: redundant owner user ID for simpler filtering and cleanup.
- `role`: `user`, `assistant`, or `system`.
- `content`: message content, stored as `longtext`.
- `status`: `0` normal, `1` generating, `2` failed.
- `error_message`: failure reason for failed assistant messages.
- `create_time`: message creation time.

Add `sys_config` rows:

- `ai.chat.baseUrl`
- `ai.chat.apiKey`
- `ai.chat.model`
- `ai.chat.temperature`
- `ai.chat.timeoutSeconds`

The API key is sensitive. The backend reads the real value for requests. The frontend must not casually expose the plaintext value; if the generic system-parameter page is reused, implementation should avoid adding any AI-specific plaintext display.

Add `sys_menu` rows:

- Menu under the existing System Tools menu: AI Chat, path `tool/aiChat/index`.
- Permissions: `tool:aiChat:list`, `tool:aiChat:send`, `tool:aiChat:remove`.

## Backend API

Use `/tool/aiChat` as the base path.

- `GET /tool/aiChat/session/list`: page current user's sessions.
- `POST /tool/aiChat/session`: create an empty session.
- `GET /tool/aiChat/session/{sessionId}/messages`: list messages for one current-user session.
- `DELETE /tool/aiChat/session/{sessionIds}`: delete current-user sessions.
- `POST /tool/aiChat/session/{sessionId}/message/stream`: send a user message in an existing session and stream the assistant response.
- `POST /tool/aiChat/message/stream`: send without a session; the backend creates a session and streams the assistant response.

Streaming responses use `text/event-stream`.

Server events:

- `session`: emitted when a session is created automatically. Includes `sessionId` and `title`.
- `message`: includes the persisted user message ID and assistant message ID.
- `delta`: assistant response text fragment.
- `done`: generation completed.
- `error`: generation failed.

## Streaming Flow

1. Receive and validate user input.
2. Create or validate the session for the current user.
3. Persist the user message.
4. Persist an assistant message with status `1` generating.
5. Load the latest context messages for the session.
6. Call the model adapter with `stream: true`.
7. For each model delta, emit a `delta` event and append the text to an in-memory buffer.
8. On normal completion, update the assistant message content and mark status `0`.
9. On provider, network, parser, or stream error, mark the assistant message status `2`, store `error_message`, and emit an `error` event.

Context strategy:

- Send only recent session messages to the model.
- Default history window is 20 messages.
- Send only `role` and `content`.
- Do not implement token-accurate trimming in the first version.

Stop behavior:

- The frontend stop button closes the current request connection.
- The backend should stop reading the provider stream when it detects disconnection.
- If partial content exists, implementation may either persist it with a failed status or persist the partial assistant content with a clear failure message. The chosen behavior must be tested and documented in the task plan.

## Frontend UX

The page follows the existing RuoYi and Element UI management style.

- Left panel: session list with create, select, and delete actions.
- Right panel: message area with visual distinction between user and assistant messages.
- Bottom input: message box, send button, stop button.
- Sending state: prevent duplicate sends while a stream is active.
- Streaming state: append assistant content as `delta` events arrive.
- Error state: show the failed assistant message in place and keep the user message.
- Refresh state: reload persisted sessions and messages after page refresh.

Permissions:

- Without `tool:aiChat:list`, the menu/page is unavailable.
- Without `tool:aiChat:send`, sending is disabled or rejected.
- Without `tool:aiChat:remove`, deleting sessions is unavailable.

## Verification

Backend verification:

- Service tests cover current-user session ownership checks.
- Service tests cover user and assistant message persistence.
- Configuration tests cover missing `baseUrl`, `apiKey`, or `model`.
- Adapter tests cover streaming callback behavior using a controlled fake stream.
- Error tests cover failed model calls and assistant message failure persistence.

Frontend and integration verification:

- Frontend build succeeds.
- SQL initializes the menu under the existing System Tools menu.
- Admin can open AI Chat.
- With valid `sys_config`, a user can send a message and see streaming deltas.
- Refreshing the page preserves session and message history.
- A user cannot read another user's sessions.
- Provider failure persists the user message, persists a failed assistant message, and shows an error in the UI.

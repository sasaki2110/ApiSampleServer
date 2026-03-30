# Phase 2: フロントエンド API 接続（Vue）

## 目的

受注登録画面のモックデータ（`src/constants/mockData.ts`）とモック保存処理（`console.info` + `alert`）を、Phase 1 で作成した Spring Boot の REST API に差し替える。

---

## 前提

- Phase 1（バックエンド）が完了し、以下の API が動いていること
  - `GET /api/masters/parties`
  - `GET /api/masters/products`
  - `POST /api/orders`
- フロントの既存 API クライアント（`src/api/client.ts`）と JWT 認証基盤がそのまま使える
- 画面エンジン（`ScreenWorkspaceView.vue`）の構造は変えない

---

## 作業手順

### ステップ 2.1: API クライアントに関数を追加

**ファイル**: `src/api/client.ts`

以下の3関数を追加する。

```typescript
export type CodeMasterItem = {
  code: string
  name: string
}

export async function fetchParties(): Promise<CodeMasterItem[]> {
  const url = `${baseUrl}/api/masters/parties`
  const res = await fetch(url, { headers: authHeader() })
  if (!res.ok) throw new Error(`fetchParties failed: ${res.status}`)
  return (await res.json()) as CodeMasterItem[]
}

export async function fetchProducts(): Promise<CodeMasterItem[]> {
  const url = `${baseUrl}/api/masters/products`
  const res = await fetch(url, { headers: authHeader() })
  if (!res.ok) throw new Error(`fetchProducts failed: ${res.status}`)
  return (await res.json()) as CodeMasterItem[]
}

export type OrderCreateRequest = {
  contractPartyCode: string
  deliveryPartyCode: string
  deliveryLocation: string
  dueDate: string
  forecastNumber: string
  lines: {
    productCode: string
    productName: string
    quantity: number
    unitPrice: number
    amount: number
  }[]
}

export type OrderCreateResponse = {
  orderId: number
  orderNumber: string
  message: string
}

export async function createOrder(body: OrderCreateRequest): Promise<OrderCreateResponse> {
  const url = `${baseUrl}/api/orders`
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify(body),
  })
  if (!res.ok) throw new Error(`createOrder failed: ${res.status}`)
  return (await res.json()) as OrderCreateResponse
}
```

---

### ステップ 2.2: 画面マウント時にマスタデータを API から取得

**現状**: `PARTIES` と `PRODUCTS` は `src/constants/mockData.ts` から直接 import している。

**変更後**: 画面マウント時（`onMounted` またはルート遷移時）に `fetchParties()` / `fetchProducts()` を呼び、`ref` に格納する。

**変更箇所**: `src/views/ScreenWorkspaceView.vue`

```typescript
// 変更前
import { PARTIES, PRODUCTS } from '@/constants/mockData'

// 変更後
import { fetchParties, fetchProducts, type CodeMasterItem } from '@/api/client'

const parties = ref<CodeMasterItem[]>([])
const products = ref<CodeMasterItem[]>([])

onMounted(async () => {
  // 既存の初期化処理...
  try {
    const [p, pr] = await Promise.all([fetchParties(), fetchProducts()])
    parties.value = p
    products.value = pr
  } catch (e) {
    console.error('マスタ取得失敗:', e)
    // フォールバック: 空配列のまま（または mockData を使う）
  }
})
```

テンプレート内で `PARTIES` → `parties`、`PRODUCTS` → `products` に置き換える。

**動作確認ポイント**:
- 契約先・納入先のプルダウンに API から取得した取引先が表示される
- 製品コードの候補リストに API から取得した製品が表示される
- API が落ちていてもエラーで画面が壊れない（空配列フォールバック）

---

### ステップ 2.3: 保存処理を POST API に差し替え

**現状**: `handleSave()` 内で `console.info` + `alert('保存しました')` + `handleNew()` している。

**変更後**: `createOrder()` を呼び、成功時にレスポンスの受注番号を表示する。

**変更箇所**: `ScreenWorkspaceView.vue` の `handleSave()` 関数（受注の分岐内）

```typescript
// 変更前
console.info('[保存モック]', { header, lines })
alert('保存しました（コンソールにモック出力）')

// 変更後
try {
  const result = await createOrder({
    contractPartyCode: (orderHeader.contractParty as { code?: string } | null)?.code ?? '',
    deliveryPartyCode: (orderHeader.deliveryParty as { code?: string } | null)?.code ?? '',
    deliveryLocation: String(orderHeader.deliveryLocation ?? ''),
    dueDate: String(orderHeader.dueDate ?? ''),
    forecastNumber: String(orderHeader.forecastNumber ?? ''),
    lines: orderRowData.value
      .filter((r) => isOrderLineFilled(r))
      .map((r) => ({
        productCode: r.productCode,
        productName: r.productName,
        quantity: r.quantity,
        unitPrice: r.unitPrice,
        amount: r.amount,
      })),
  })
  alert(`受注を登録しました（受注番号: ${result.orderNumber}）`)
} catch (e) {
  console.error('受注登録失敗:', e)
  alert('受注の登録に失敗しました。コンソールを確認してください。')
  return // 失敗時は初期化しない
}
```

**動作確認ポイント**:
- F12 で保存すると、API が呼ばれ、受注番号がアラートに表示される
- H2 Console で `ORDER_HEADER` / `ORDER_LINE` テーブルにデータが入っていること
- 認証トークンが切れている場合にエラーメッセージが出ること

---

### ステップ 2.4: `mockData.ts` の段階的廃止

API 接続が安定したら、`src/constants/mockData.ts` の `PARTIES` と `PRODUCTS` の直接参照を順次削除する。

ただし以下の用途では残してもよい：
- API が落ちているときのフォールバック（開発時の利便性）
- `CodeMasterItem` 型の定義元（API クライアント側に移動したならそちらを使う）

最終的に `mockData.ts` から型以外のエクスポートがなくなったら、ファイルごと削除する。

---

### ステップ 2.5: 画面定義 Markdown への API 情報追記

`docs/screen-specs/order-new.md` に「マスタ・データソース」と「画面アクション → API」セクションを追加する。

追記内容の詳細は本ステップの完了条件を参照。

---

## 完了条件

1. **契約先・納入先のプルダウンが `GET /api/masters/parties` から取得したデータで表示される**
2. **製品コードの候補リストが `GET /api/masters/products` から取得したデータで表示される**
3. **F12（保存）で `POST /api/orders` が呼ばれ、H2 にデータが永続化される**
4. **保存成功時にレスポンスの受注番号がユーザーに表示される**
5. **`docs/screen-specs/order-new.md` に API 情報が記載されている**
6. **`npm run build` が成功する**

---

## 手動チェックリスト

- [ ] 画面表示時にブラウザの DevTools Network で `GET /api/masters/parties` と `GET /api/masters/products` が呼ばれている
- [ ] 契約先プルダウンに API から取得した10件が表示される
- [ ] 製品コード候補リストに API から取得した10件が表示される
- [ ] 全項目を入力して F12 → アラートに受注番号が表示される
- [ ] H2 Console（`http://localhost:8080/h2-console`）で `SELECT * FROM ORDER_HEADER` にデータがある
- [ ] H2 Console で `SELECT * FROM ORDER_LINE` にデータがある
- [ ] バックエンドを停止した状態で画面を開いても、エラーで画面が壊れない
- [ ] Phase 1 チェックリスト（Enter 移動、F キーなど）が引き続き合格する

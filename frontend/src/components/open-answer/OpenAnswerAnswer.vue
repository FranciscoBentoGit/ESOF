<!--
Used on:
  - QuestionComponent.vue
  - ResultComponent.vue
-->

<template>
  <v-row>
    <v-col cols="10">
      <v-textarea
        v-model="answerDetails.response"
        :label="Response"
        :data-cy="questionResponseTextArea"
        rows="1"
        auto-grow
      ></v-textarea>
    </v-col>
  </v-row>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model, Emit } from 'vue-property-decorator';
import OpenAnswerStatementQuestionDetails from '@/models/statement/questions/OpenAnswerStatementQuestionDetails';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import Image from '@/models/management/Image';
import OpenAnswerStatementAnswerDetails from '@/models/statement/questions/OpenAnswerStatementAnswerDetails';
import OpenAnswerStatementCorrectAnswerDetails from '@/models/statement/questions/OpenAnswerStatementCorrectAnswerDetails';
@Component
export default class OpenAnswerAnswer extends Vue {
  @Prop(OpenAnswerStatementQuestionDetails)
  readonly questionDetails!: OpenAnswerStatementQuestionDetails;
  @Prop(OpenAnswerStatementAnswerDetails)
  answerDetails!: OpenAnswerStatementAnswerDetails;
  @Prop(OpenAnswerStatementCorrectAnswerDetails)
  readonly correctAnswerDetails?: OpenAnswerStatementCorrectAnswerDetails;
  get isReadonly() {
    return !!this.correctAnswerDetails;
  }
  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }
}
</script>
